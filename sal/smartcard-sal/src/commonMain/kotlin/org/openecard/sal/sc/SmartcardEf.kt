package org.openecard.sal.sc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.cif.definition.dataset.DatasetType
import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.command.FileControlInformation
import org.openecard.sc.apdu.command.ReadBinary
import org.openecard.sc.apdu.command.ReadRecord
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.info.EfStructure
import org.openecard.sc.iface.info.Fcp
import org.openecard.sc.iface.info.FileInfo
import org.openecard.utils.common.mergeToArray
import org.openecard.utils.common.toUShort
import org.openecard.utils.serialization.toPrintable

private val log = KotlinLogging.logger { }

class SmartcardEf
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val channel: CardChannel,
		val path: UByteArray,
		val shortEf: UByte?,
		/**
		 * Dataset type, if known.
		 * If this value is not set, the SAL will detect the correct type by itself.
		 */
		var type: DatasetType? = null,
		/**
		 * Use SAL dataset to reflect state updates.
		 */
		val salDataset: SmartcardDataset? = null,
	) {
		private var efStructure: EfStructure? = null
		private var fileInfo: FileInfo? = null

		private val isSelected: Boolean get() {
			return salDataset?.let { it.application.device.isSelectedDataset(it) } ?: false
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		private fun select(forceReadFcp: Boolean = false) {
			// determine if we need the select to read FCP
			val readFcp = fileInfo == null || forceReadFcp

			// only select when needed
			if ((!isSelected && shortEf == null) || readFcp) {
				val select =
					if (path.size == 2) {
						Select.selectEfIdentifier(path.toUShort(0))
					}	else {
						Select.selectPathRelative(path)
					}

				if (readFcp) {
					try {
						log.debug { "Selecting file ${path.toPrintable()} with FCP" }
						val selectFcp = select.copy(fileControlInfo = FileControlInformation.FCP)
						salDataset?.let { it.application.device.setSelectedDataset(it) }
						val fi = checkNotNull(selectFcp.transmit(channel))
						// determine type
						updateType(fi)
						fileInfo = fi
						return
					} catch (ex: Exception) {
						log.warn(ex) { "Failed to select file with FCP" }
						fileInfo = FileInfo.Unknown(UByteArray(0))
					}
				}

				select.transmit(channel)
				salDataset?.let { it.application.device.setSelectedDataset(it) }
			}
		}

		private fun updateType(fileInfo: FileInfo) {
			when (fileInfo) {
				is Fcp -> {
					val struct = fileInfo.fileDescriptor?.fdByte?.efStructure
					efStructure = struct
					when (struct) {
						EfStructure.TRANSPARENT,
						-> {
							type = DatasetType.TRANSPARENT
						}

						EfStructure.LINEAR_FIXED_ANY,
						EfStructure.LINEAR_FIXED_TLV,
						EfStructure.LINEAR_VARIABLE_ANY,
						EfStructure.LINEAR_VARIABLE_TLV,
						-> {
							type = DatasetType.RECORD
						}

						EfStructure.CYCLIC_FIXED_ANY,
						EfStructure.CYCLIC_FIXED_TLV,
						-> {
							type = DatasetType.RING
						}

						EfStructure.TLV_BER, EfStructure.TLV_SIMPLE,
						-> {
							type = DatasetType.DATA_OBJECT
						}

						else -> {}
					}
				}

				else -> {}
			}
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		fun read(): UByteArray {
			select()

			return when (type) {
				DatasetType.TRANSPARENT -> readTransparent()
				DatasetType.RECORD -> readRecords()
				DatasetType.RING -> TODO("Implement")
				DatasetType.DATA_OBJECT -> TODO("Implement")
				null -> readTrying()
			}
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		private fun readTrying(): UByteArray =
			runCatching {
				readTransparent().also {
					setFileTypeForced(DatasetType.TRANSPARENT)
				}
			}.recover {
				readRecords().also {
					setFileTypeForced(DatasetType.RECORD)
				}
			}.getOrThrow()

		private fun setFileTypeForced(type: DatasetType) {
			if (this.type == null) {
				this.type = type
			}
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		private fun readTransparent(): UByteArray {
			val extLen =
				channel.card
					.getCapabilities()
					?.commandCoding
					?.supportsExtendedLength ?: false
			val shortEf = shortEf
			val apdu =
				// only use short ef if the file is not already selected
				if (shortEf != null && !isSelected) {
					ReadBinary.readShortEf(shortEf, forceExtendedLength = extLen)
				} else {
					ReadBinary.readCurrentEf(forceExtendedLength = extLen)
				}
			return readUntilEof(apdu)
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		private fun readUntilEof(apdu: ReadBinary): UByteArray {
			val numBytes =
				when (fileInfo) {
					is Fcp -> (fileInfo as Fcp).numBytes
					else -> null
				}

			var readBytes = 0L
			val resultBytes = mutableListOf<UByteArray>()
			var nextApdu = apdu
			do {
				try {
					val nextBytes = nextApdu.transmit(channel)
					resultBytes.add(nextBytes)
					readBytes += nextBytes.size
					if (readBytes == 0L) {
						// file is empty
						break
					}

					if (readBytes == (numBytes ?: -1)) {
						// we read all bytes the file claims to contain
						break
					} else {
						// maybe there is more, build successive apdu
						nextApdu = nextApdu.copy(offset = readBytes.toULong())
					}
				} catch (ex: ApduProcessingError) {
					// APDU could not be executed
					if (readBytes == 0L) {
						// no data means the first read failed, which should definitely not the case
						throw ex
					} else if (readBytes > 0) {
						// we could read some bytes, but then we hit the error
						// we assume this is EOF, but in reality there is no clear error in this case, so this is guesswork
						// some cards raise 6B00 (P1-P2 error)
						// what I really don't get is why the file size of the FCP is incorrect
						break
					} else {
						// no good case left, signal the error
						throw ex
					}
				}
			} while (true)

			return resultBytes.mergeToArray()
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		private fun readRecords(): UByteArray {
			val extLen =
				channel.card
					.getCapabilities()
					?.commandCoding
					?.supportsExtendedLength ?: false
			val shortEf = shortEf
			val apdu =
				// only use short ef if the file is not already selected
				if (shortEf != null && !isSelected) {
					ReadRecord.readAllRecordsIndividual(shortEf = shortEf, forceExtendedLength = extLen)
				} else {
					ReadRecord.readAllRecordsIndividual(forceExtendedLength = extLen)
				}
			return apdu.transmit(channel)
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		fun write(): UByteArray {
			select()
			TODO("Not yet implemented")
		}
	}
