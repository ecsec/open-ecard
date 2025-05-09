/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 */
package org.openecard.common.apdu.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.apdu.ReadBinary
import org.openecard.common.apdu.ReadRecord
import org.openecard.common.apdu.Select
import org.openecard.common.apdu.UpdateRecord
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.common.CardCommandStatus
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.apdu.common.TrailerConstants
import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.iso7816.FCP
import org.openecard.common.util.ShortUtils.toByteArray
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.jvm.Throws

private val LOG = KotlinLogging.logger { }

/**
 * Utility class for elementary file operations with smart cards.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Simon Potzernheim
 * @author Tobias Wich
 */
object CardUtils {
	/**
	 * Selects the Master File.
	 *
	 * @param dispatcher Dispatcher
	 * @param slotHandle Slot handle
	 * @throws APDUException
	 */
	@JvmStatic
	@Throws(APDUException::class)
	fun selectMF(
		dispatcher: Dispatcher,
		slotHandle: ByteArray?,
	) {
		val selectMF: CardCommandAPDU = Select.MasterFile()
		selectMF.transmit(dispatcher, slotHandle)
	}

	/**
	 * Selects a File.
	 *
	 * @param dispatcher Dispatcher
	 * @param slotHandle Slot handle
	 * @param fileID File ID
	 * @return The CardResponseAPDU from the selection of the file
	 * @throws APDUException
	 */
	@JvmStatic
	@Throws(APDUException::class)
	fun selectFile(
		dispatcher: Dispatcher?,
		slotHandle: ByteArray?,
		fileID: Short,
	): CardResponseAPDU = selectFile(dispatcher, slotHandle, toByteArray(fileID))

	/**
	 * Selects a File.
	 *
	 * @param dispatcher Dispatcher
	 * @param slotHandle Slot handle
	 * @param fileID File ID
	 * @return CardREsponseAPDU containing the File Control Parameters
	 * @throws APDUException
	 */
	@JvmStatic
	@Throws(APDUException::class)
	fun selectFile(
		dispatcher: Dispatcher?,
		slotHandle: ByteArray?,
		fileID: ByteArray,
	): CardResponseAPDU = selectFileWithOptions(dispatcher, slotHandle, fileID, null, FileControlParameters.NONE)

	/**
	 * Select a file with different options.
	 *
	 * @param dispatcher The Dispatcher for dispatching of the card commands.
	 * @param slotHandle The SlotHandle which identifies the card terminal.
	 * @param fileIdOrPath File identifier or path to the file to select.
	 * @param responses List of byte arrays with the trailers which should not thrown as errors.
	 * @param resultType Int value which indicates whether the select should be performed with a request of the FCP, FCI,
	 * FMD or without any data. There are four public variables available in this class to use.
	 * @return A CardResponseAPDU object with the requested response data.
	 * @throws APDUException Thrown if the selection of a file failed.
	 */
	@JvmStatic
	@Throws(APDUException::class)
	fun selectFileWithOptions(
		dispatcher: Dispatcher?,
		slotHandle: ByteArray?,
		fileIdOrPath: ByteArray,
		responses: MutableList<ByteArray?>?,
		resultType: FileControlParameters,
	): CardResponseAPDU {
		var responses = responses
		var selectFile: Select
		var result: CardResponseAPDU? = null

		if (fileIdOrPath.isEmpty()) {
			throw APDUException("fileIdOrPath is empty, cannot resolve file or path")
		}

		// respect the possibility that fileID could be a path
		var i = 0
		while (i < fileIdOrPath.size) {
			if (fileIdOrPath[i] == 0x3F.toByte() && fileIdOrPath[i + 1] == 0x00.toByte() && i == 0) {
				selectFile = Select.MasterFile()
				i += 2
			} else if (i == fileIdOrPath.size - 2) {
				selectFile =
					Select.ChildFile(
						byteArrayOf(
							fileIdOrPath[i],
							fileIdOrPath[i + 1],
						),
					)
				when (resultType) {
					FileControlParameters.NONE -> selectFile.setNoMetadata()
					FileControlParameters.FCP -> selectFile.setFCP()
					FileControlParameters.FCI -> selectFile.setFCI()
					FileControlParameters.FMD -> selectFile.setFMD()
					else -> throw APDUException("There is no value associated with the returnType value $resultType")
				}

				i += 2
			} else {
				selectFile =
					Select.ChildDirectory(
						byteArrayOf(
							fileIdOrPath[i],
							fileIdOrPath[i + 1],
						),
					)
				i += 2
			}

			if (responses == null) {
				// not all cards, e.g. Estonian id card, support P1 = 00 and DataFile filled with MF Fid so work around this
				if (i == 2 && fileIdOrPath[0] == 0x3F.toByte() && fileIdOrPath[1] == 0x00.toByte()) {
					responses = ArrayList()
					responses.add(byteArrayOf(0x90.toByte(), 0x00.toByte()))
					responses.add(byteArrayOf(0x67.toByte(), 0x00.toByte()))
					responses.add(byteArrayOf(0x6A.toByte(), 0x86.toByte()))
				}
				result = selectFile.transmit(dispatcher!!, slotHandle, responses)

				if (!result.trailer.contentEquals(
						byteArrayOf(
							0x90.toByte(),
							0x00.toByte(),
						),
					) &&
					i == 2 &&
					fileIdOrPath[0] == 0x3F.toByte() &&
					fileIdOrPath[1] == 0x00.toByte()
				) {
					selectFile = Select(0x00.toByte(), 0x0c.toByte())
					result = selectFile.transmit(dispatcher, slotHandle, responses)

					// if the result is still not 9000 the card probably does not support single directory/file selection
					// so lets try selection by path
					if (!result.trailer.contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte())) &&
						fileIdOrPath.size > 2
					) {
						selectFile = Select.AbsolutePath(fileIdOrPath)
						result = selectFile.transmit(dispatcher, slotHandle)
						if (result.trailer.contentEquals(TrailerConstants.Success.OK())) {
							return result
						}
					}
				}
			} else {
				result = selectFile.transmit(dispatcher!!, slotHandle, responses)
			}
		}

		return result!!
	}

	/**
	 * Select an application by it's file identifier.
	 *
	 * @param dispatcher The message dispatcher for the interaction with the card.
	 * @param slotHandle
	 * @param fileID File identitfier of an application or a path to the application.
	 * @return The [CardResponseAPDU] from the last select which means the select of the application to select.
	 * @throws APDUException
	 */
	@JvmStatic
	@Throws(APDUException::class)
	fun selectApplicationByFID(
		dispatcher: Dispatcher?,
		slotHandle: ByteArray?,
		fileID: ByteArray,
	): CardResponseAPDU? {
		var selectApp: Select
		var result: CardResponseAPDU? = null

		// respect the possibility that fileID could be a path
		var i = 0
		while (i < fileID.size) {
			if (fileID[i] == 0x3F.toByte() && fileID[i + 1] == 0x00.toByte() && i == 0 && i + 1 == 1) {
				selectApp = Select.MasterFile()
				i += 2
			} else {
				selectApp = Select.ChildDirectory(byteArrayOf(fileID[i], fileID[i + 1]))
				selectApp.setLE(0xFF.toByte())
				selectApp.setFCP()
				i += 2
			}

			result = selectApp.transmit(dispatcher!!, slotHandle)
		}

		return result
	}

	/**
	 * Select an application by the application identifier.
	 * This method requests the FCP of the application.
	 *
	 * @param dispatcher
	 * @param slotHandle
	 * @param aid Application identifier
	 * @return Response APDU of the select command.
	 * @throws APDUException Thrown in case there was an error while processing the command APDU.
	 */
	@JvmStatic
	@Throws(APDUException::class)
	fun selectApplicationByAID(
		dispatcher: Dispatcher?,
		slotHandle: ByteArray?,
		aid: ByteArray?,
	): CardResponseAPDU {
		val selectApp = Select(0x04.toByte(), 0x04.toByte())
		selectApp.data = aid!!
		selectApp.setLE(0xFF.toByte())
		val result = selectApp.transmit(dispatcher!!, slotHandle)
		return result
	}

	@JvmStatic
	@Throws(APDUException::class)
	fun readFile(
		fcp: FCP?,
		dispatcher: Dispatcher,
		slotHandle: ByteArray?,
		readWithExtendedLength: Boolean,
	): ByteArray = readFile(fcp, null, dispatcher, slotHandle, readWithExtendedLength)

	/**
	 * Reads a file.
	 *
	 * @param dispatcher Dispatcher
	 * @param slotHandle Slot handle
	 * @param fcp File Control Parameters, may be null
	 * @param shortEf Short EF identifier, may be null
	 * @return File content
	 * @throws APDUException
	 */
	@JvmStatic
	@Throws(APDUException::class)
	fun readFile(
		fcp: FCP?,
		shortEf: Byte?,
		dispatcher: Dispatcher,
		slotHandle: ByteArray?,
		readWithExtendedLength: Boolean,
	): ByteArray {
		val baos = ByteArrayOutputStream()
		// Read as much bytes per APDU as possible
		var length = (if (readWithExtendedLength) 0xFFFF else 0xFF).toShort()
		var numToRead: Short = -1 // -1 indicates I don't know
		if (fcp != null) {
			val fcpNumBytes = fcp.numBytes
			if (fcpNumBytes != null) {
				// more than short is not possible and besides that very unrealistic
				numToRead = fcpNumBytes.toShort()
				// reduce readout size
				if (numToRead < 255) {
					length = numToRead.toByte().toShort()
				}
			}
		}

		val isRecord = isRecordEF(fcp)
		var i = (if (isRecord) 1 else 0).toByte() // records start at index 1
		var numRead: Short = 0

		try {
			var response: CardResponseAPDU
			var trailer: ByteArray?
			var lastNumRead = 0
			var goAgain: Boolean
			do {
				if (!isRecord) {
					val readBinary: CardCommandAPDU =
						if (shortEf != null) {
							if (numRead > 0xFF) {
								ReadBinary(shortEf.toShort(), numRead, length)
							} else {
								ReadBinary(shortEf, numRead.toByte(), length)
							}
						} else {
							ReadBinary(numRead, length)
						}
					// 0x6A84 code for the estonian identity card. The card returns this code
					// after the last read process.
					response =
						readBinary.transmit(
							dispatcher,
							slotHandle,
							CardCommandStatus.response(
								0x9000,
								0x6282,
								0x6A84,
								0x6A83,
								0x6A86,
								0x6B00,
							),
						)
				} else {
					val readRecord: CardCommandAPDU =
						if (shortEf != null) {
							ReadRecord(shortEf, i)
						} else {
							ReadRecord(i)
						}
					response =
						readRecord.transmit(
							dispatcher,
							slotHandle,
							CardCommandStatus.response(
								0x9000,
								0x6282,
								0x6A84,
								0x6A83,
							),
						)
				}

				trailer = response.trailer
				if (!trailer.contentEquals(byteArrayOf(0x6A.toByte(), 0x84.toByte())) &&
					!trailer.contentEquals(
						byteArrayOf(0x6A.toByte(), 0x83.toByte()),
					) &&
					!trailer.contentEquals(byteArrayOf(0x6A.toByte(), 0x86.toByte()))
				) {
					val data = response.data
					// some cards are just pure shit and return 9000 when no bytes have been read
					baos.write(data)
					lastNumRead = data.size
					numRead = (numRead + lastNumRead).toShort()
				}
				i++

				// update length value
				goAgain = response.isNormalProcessed &&
					lastNumRead != 0 ||
					(trailer.contentEquals(byteArrayOf(0x62.toByte(), 0x82.toByte())) && isRecord)
				if (goAgain && numToRead.toInt() != -1) {
					// we have a limit, enforce it
					val remainingBytes = (numToRead - numRead).toShort()
					if (remainingBytes <= 0) {
						goAgain = false
					} else if (remainingBytes < 255) {
						// update length when we reached the area below 255
						length = remainingBytes.toByte().toShort()
					}
				}
			} while (goAgain)
			baos.close()
		} catch (e: IOException) {
			throw APDUException(e)
		}

		return baos.toByteArray()
	}

	/**
	 * Selects and reads a file.
	 *
	 * @param dispatcher Dispatcher
	 * @param slotHandle Slot handle
	 * @param fileID File ID
	 * @return File content
	 * @throws APDUException
	 */
	@JvmStatic
	@Throws(APDUException::class)
	fun selectReadFile(
		dispatcher: Dispatcher,
		slotHandle: ByteArray?,
		fileID: ByteArray,
	): ByteArray {
		var fcp: FCP? = null
		if (!isShortEFIdentifier(fileID)) {
			val selectResponse = selectFileWithOptions(dispatcher, slotHandle, fileID, null, FileControlParameters.FCP)
			try {
				fcp = FCP(selectResponse.data)
			} catch (e: TLVException) {
				LOG.warn(e) { "Couldn't get File Control Parameters from Select response." }
			}
		}
		return readFile(fcp, dispatcher, slotHandle, false)
	}

	private fun isRecordEF(fcp: FCP?): Boolean {
		if (fcp == null) {
			// TODO inspect EF.ATR as described in ISO/IEC 7816-4 Section 8.4
			return false
		} else {
			val dataElements = fcp.dataElements
			return dataElements.isLinear || dataElements.isCyclic
		}
	}

	@JvmStatic
	fun isShortEFIdentifier(fileID: ByteArray): Boolean = fileID.size == 1

	@JvmStatic
	fun writeFile(
		dispatcher: Dispatcher,
		slotHandle: ByteArray,
		fileID: ByteArray,
		data: ByteArray,
	) {
		val selectResponse = selectFile(dispatcher, slotHandle, fileID)
		var fcp: FCP? = null
		try {
			fcp = FCP(selectResponse.data)
		} catch (e: TLVException) {
			LOG.warn(e) { "Couldn't get File Control Parameters from Select response." }
		}
		writeFile(fcp, dispatcher, slotHandle, data)
	}

	private fun writeFile(
		fcp: FCP?,
		dispatcher: Dispatcher,
		slotHandle: ByteArray,
		data: ByteArray,
	) {
		if (isRecordEF(fcp)) {
			val updateRecord = UpdateRecord(data)
			updateRecord.transmit(dispatcher, slotHandle)
		} else {
			// TODO implement writing for non record files
			throw UnsupportedOperationException("Not yet implemented.")
		}
	}
}
