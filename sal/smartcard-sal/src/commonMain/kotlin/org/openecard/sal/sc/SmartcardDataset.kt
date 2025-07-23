package org.openecard.sal.sc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.dataset.DataSetDefinition
import org.openecard.cif.definition.dataset.DatasetType
import org.openecard.sal.iface.Dataset
import org.openecard.sal.iface.MissingAuthentication
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.sc.acl.missingAuthentications
import org.openecard.sc.apdu.command.FileControlInformation
import org.openecard.sc.apdu.command.ReadBinary
import org.openecard.sc.apdu.command.ReadRecord
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.info.EfStructure
import org.openecard.sc.iface.info.Fcp
import org.openecard.sc.iface.info.FileInfo
import org.openecard.utils.common.throwIf
import org.openecard.utils.common.toUShort

private val log = KotlinLogging.logger { }

class SmartcardDataset(
	override val name: String,
	override val application: SmartcardApplication,
	val ds: DataSetDefinition,
	val readAcl: BoolTreeOr<BoolTreeLeaf>,
	val writeAcl: BoolTreeOr<BoolTreeLeaf>,
) : Dataset {
	override val missingReadAuthentications: MissingAuthentications
		get() = readAcl.missingAuthentications(application.device)
	override val missingWriteAuthentications: MissingAuthentications
		get() = writeAcl.missingAuthentications(application.device)

	private var type: DatasetType? = ds.type
	private var efStructure: EfStructure? = null

	// set to unknown if we already know our type
	@OptIn(ExperimentalUnsignedTypes::class)
	private var fileInfo: FileInfo? = type?.let { FileInfo.Unknown(UByteArray(0)) }

	private val channel: CardChannel
		get() = application.channel

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun select(forceReadFcp: Boolean = false) {
		// determine if we need the select to read FCP
		val readFcp = fileInfo == null || forceReadFcp

		// only select when needed
		if ((!application.device.isSelectedDataset(this) && ds.shortEf == null) || readFcp) {
			val select =
				if (ds.path.v.size == 2) {
					Select.selectEfIdentifier(ds.path.v.toUShort(0))
				}	else {
					Select.selectPathRelative(ds.path.v)
				}

			if (readFcp) {
				try {
					log.debug { "Selecting file ${ds.name} with FCP" }
					val selectFcp = select.copy(fileControlInfo = FileControlInformation.FCP)
					application.device.setSelectedDataset(this)
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
			application.device.setSelectedDataset(this)
		}
	}

	private fun updateType(fileInfo: FileInfo) {
		when (fileInfo) {
			is Fcp -> {
				val struct = fileInfo.fileDescriptor?.fdByte?.efStructure
				efStructure = struct
				when (struct) {
					EfStructure.TRANSPARENT,
					-> type = DatasetType.TRANSPARENT
					EfStructure.LINEAR_FIXED_ANY,
					EfStructure.LINEAR_FIXED_TLV,
					EfStructure.LINEAR_VARIABLE_ANY,
					EfStructure.LINEAR_VARIABLE_TLV,
					-> type = DatasetType.RECORD
					EfStructure.CYCLIC_FIXED_ANY,
					EfStructure.CYCLIC_FIXED_TLV,
					-> type = DatasetType.RING
					EfStructure.TLV_BER, EfStructure.TLV_SIMPLE,
					-> type = DatasetType.DATA_OBJECT
					else -> {}
				}
			}
			else -> {}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun read(): UByteArray =
		mapSmartcardError {
			throwIf(!missingReadAuthentications.isSolved) { MissingAuthentication("Read ACL is not satisfied") }
			select()

			when (type) {
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
			channel.card.capabilities
				?.commandCoding
				?.supportsExtendedLength ?: false
		val shortEf = ds.shortEf
		val apdu =
			// only use short ef if the file is not already selected
			if (shortEf != null && !application.device.isSelectedDataset(this)) {
				ReadBinary.readShortEf(shortEf, forceExtendedLength = extLen)
			} else {
				ReadBinary.readCurrentEf(forceExtendedLength = extLen)
			}
		return apdu.transmit(channel)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun readRecords(): UByteArray {
		val extLen =
			channel.card.capabilities
				?.commandCoding
				?.supportsExtendedLength ?: false
		val shortEf = ds.shortEf
		val apdu =
			// only use short ef if the file is not already selected
			if (shortEf != null && !application.device.isSelectedDataset(this)) {
				ReadRecord.readAllRecordsIndividual(shortEf = shortEf, forceExtendedLength = extLen)
			} else {
				ReadRecord.readAllRecordsIndividual(forceExtendedLength = extLen)
			}
		return apdu.transmit(channel)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun write(): UByteArray =
		mapSmartcardError {
			throwIf(!missingWriteAuthentications.isSolved) { MissingAuthentication("Write ACL is not satisfied") }
			select()
			TODO("Not yet implemented")
		}
}
