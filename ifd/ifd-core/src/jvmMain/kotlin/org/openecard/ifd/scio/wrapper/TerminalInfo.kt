/****************************************************************************
 * Copyright (C) 2015-2018 ecsec GmbH.
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
 ***************************************************************************/
package org.openecard.ifd.scio.wrapper

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.BioSensorCapabilityType
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType
import iso.std.iso_iec._24727.tech.schema.IFDStatusType
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType
import iso.std.iso_iec._24727.tech.schema.SlotStatusType
import org.openecard.common.ECardConstants
import org.openecard.common.ifd.PACECapabilities
import org.openecard.common.ifd.scio.NoSuchTerminal
import org.openecard.common.ifd.scio.SCIOErrorCode
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.common.ifd.scio.SCIOTerminal
import org.openecard.common.util.ByteUtils
import org.openecard.ifd.scio.reader.ExecutePACERequest
import org.openecard.ifd.scio.reader.ExecutePACEResponse
import org.openecard.ifd.scio.reader.PCSCFeatures
import java.math.BigInteger

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class TerminalInfo {
	private val cm: ChannelManager
	private val term: SCIOTerminal
	private val externalChannel: Boolean
	private var channel: SingleThreadChannel? = null

	@get:Throws(SCIOException::class, InterruptedException::class)
	val featureCodes: Map<Int, Int> by lazy {
		val code = PCSCFeatures.getFeatureRequestCtlCode()
		try {
			val response = channel!!.transmitControlCommand(code, ByteArray(0))
			PCSCFeatures.featureMapFromRequest(response)
		} catch (ex: SCIOException) {
			// TODO: remove this workaround by supporting feature requests under all systems and all readers
			LOG.warn(ex) { "Unable to request features from reader." }
			mutableMapOf()
		} catch (ex: IllegalStateException) {
			LOG.warn(ex) { "Transmit control command failed due to missing card connection." }
			mutableMapOf()
		}
	}

	// capabilities entries
	private var acoustic: Boolean? = null
	private var optic: Boolean? = null
	private var dispCapRead = false
	private var dispCap: DisplayCapabilityType? = null
	private var keyCapRead = false
	private var keyCap: KeyPadCapabilityType? = null
	private var bioCapRead = false
	private var bioCap: BioSensorCapabilityType? = null

	@get:Throws(SCIOException::class, InterruptedException::class)
	val pACECapabilities: List<PACECapabilities.PACECapability>
		get() {
			val result = mutableListOf<PACECapabilities.PACECapability>()
			if (this.isConnected) {
				if (supportsPace()) {
					val ctrlCode = this.paceCtrlCode!!
					val paceFunc = ExecutePACERequest.Function.GetReaderPACECapabilities
					val getCapabilityRequest = ExecutePACERequest(paceFunc).toBytes()
					val response = channel!!.transmitControlCommand(ctrlCode, getCapabilityRequest)
					val paceResponse = ExecutePACEResponse(response)
					if (paceResponse.isError) {
						var msg = "PACE is advertised but the result iss erroneous.\n${
							paceResponse.getResult().getResultMessage().getValue()
						}"
						throw SCIOException(
							msg,
							SCIOErrorCode.SCARD_F_UNKNOWN_ERROR,
						)
					}
					val cap = PACECapabilities(paceResponse.data)
					result.addAll(cap.featuresEnum)
				}
			}
			return result
		}

	constructor(cm: ChannelManager, term: SCIOTerminal) {
		this.cm = cm
		this.term = term
		this.externalChannel = false
	}

	constructor(cm: ChannelManager, channel: SingleThreadChannel) {
		this.cm = cm
		this.term = channel.channel.card.terminal
		this.externalChannel = true
		this.channel = channel
	}

	val name: String
		get() = term.name

	val isCardPresent: Boolean
		get() {
			return try {
				term.isCardPresent
			} catch (ex: SCIOException) {
				false
			}
		}

	val isConnected: Boolean
		get() = channel != null

	@get:Throws(SCIOException::class)
	val status: IFDStatusType
		get() {
			val status = IFDStatusType()
			status.setIFDName(this.name)
			status.isConnected = true

			// set slot status type
			val stype = SlotStatusType()
			status.getSlotStatus().add(stype)
			val cardPresent = this.isCardPresent
			stype.isCardAvailable = cardPresent
			stype.setIndex(BigInteger.ZERO)
			// get card status and stuff
			if (this.isConnected) {
				val atr = channel!!.channel.card.aTR
				stype.setATRorATS(atr.bytes)
			} else if (cardPresent) {
				// not connected, but card is present
				try {
					val ch = cm.openMasterChannel(this.name)
					val atr = ch.channel.card.aTR
					stype.setATRorATS(atr.bytes)
				} catch (ex: NoSuchTerminal) {
					val msg = "Failed to connect card as terminal disappeared."
					throw SCIOException(
						msg,
						SCIOErrorCode.SCARD_E_UNKNOWN_READER,
						ex,
					)
				}
			}
			// ifd status completely constructed
			return status
		}

	@get:Throws(SCIOException::class, InterruptedException::class)
	val slotCapability: SlotCapabilityType by lazy {
		val cap = SlotCapabilityType()
		cap.setIndex(BigInteger.ZERO)

		val ifaceProto = this.interfaceProtocol
		if (ifaceProto != null) {
			cap.getProtocol().add(ifaceProto)
		}

		if (supportsPace()) {
			val capabilities = this.pACECapabilities
			val protos = buildPACEProtocolList(capabilities)
			cap.getProtocol().addAll(protos)
		}
		if (supportsPinCompare()) {
			cap.getProtocol().add(ECardConstants.Protocol.PIN_COMPARE)
		}

		cap
	}

	@get:Throws(SCIOException::class)
	val isAcousticSignal: Boolean
		get() {
			if (acoustic == null) {
				// no way to ask PCSC this question
				acoustic = false
			}
			return acoustic!!
		}

	@get:Throws(SCIOException::class)
	val isOpticalSignal: Boolean
		get() {
			if (optic == null) {
				// no way to ask PCSC this question
				optic = false
			}
			return optic!!
		}

	@get:Throws(SCIOException::class, InterruptedException::class)
	val displayCapability: DisplayCapabilityType?
		get() {
			if (!dispCapRead) {
				if (this.isConnected) {
					val features = this.featureCodes
					if (features.containsKey(PCSCFeatures.IFD_DISPLAY_PROPERTIES)) {
						val displayFeature: Int = features[PCSCFeatures.IFD_DISPLAY_PROPERTIES]!!
						val data = channel!!.transmitControlCommand(displayFeature, ByteArray(0))
						if (data.size == 4) {
							val lineLength =
								ByteUtils.toInteger(data.copyOfRange(0, 2))
							val numLines =
								ByteUtils.toInteger(data.copyOfRange(2, 4))
							if (lineLength > 0 && numLines > 0) {
								dispCap = DisplayCapabilityType()
								dispCap!!.setIndex(BigInteger.ZERO)
								dispCap!!.setColumns(BigInteger.valueOf(lineLength.toLong()))
								dispCap!!.setLines(BigInteger.valueOf(numLines.toLong()))
							}
						}
					}
					// regardless whether the data has been successfully extracted, or not, the data has been read
					dispCapRead = true
				}
			}
			return dispCap
		}

	@get:Throws(SCIOException::class, InterruptedException::class)
	val keypadCapability: KeyPadCapabilityType?
		get() {
			if (!keyCapRead) {
				if (this.isConnected) {
					// try to get the properties from the reader
					val features = this.featureCodes
					if (features.containsKey(PCSCFeatures.IFD_PIN_PROPERTIES)) {
						val pinFeature: Int = features[PCSCFeatures.IFD_PIN_PROPERTIES]!!
						val data = channel!!.transmitControlCommand(pinFeature, ByteArray(0))
						if (data.size == 4) {
							val wcdLayout = ByteUtils.toInteger(data.copyOfRange(0, 2))
							val entryValidation = data[2]
							val timeOut2 = data[3]

							// TODO: extract number of keys somehow

							// write our data structure
							keyCap = KeyPadCapabilityType()
							keyCap!!.setIndex(BigInteger.ZERO)
							keyCap!!.setKeys(BigInteger.valueOf(16))
						}
					}

					// regardless whether the data has been successfully extracted, or not, the data has been read
					keyCapRead = true
				}
			}
			return keyCap
		}

	val biosensorCapability: BioSensorCapabilityType?
		get() {
			if (!bioCapRead) {
				// TODO: read actual biosensor capability
				bioCap = null
				bioCapRead = true
			}

			return bioCap
		}

	private val interfaceProtocol: String?
		get() {
			if (this.isConnected) {
				val card = channel!!.channel.card
				val contactless = card.isContactless
				return if (contactless) {
					ECardConstants.IFD.Protocol.TYPE_A
				} else {
					ECardConstants.IFD.Protocol.T0
				}
			} else {
				return null
			}
		}

	@get:Throws(SCIOException::class, InterruptedException::class)
	private val paceCtrlCode: Int?
		get() {
			if (this.isConnected) {
				val features = this.featureCodes
				return features[PCSCFeatures.EXECUTE_PACE]
			} else {
				return null
			}
		}

	@Throws(SCIOException::class, InterruptedException::class)
	fun supportsPace(): Boolean = this.paceCtrlCode != null

	@get:Throws(SCIOException::class, InterruptedException::class)
	private val pinCompareCtrlCode: Int?
		get() {
			if (this.isConnected) {
				val features = this.featureCodes
				return features[PCSCFeatures.VERIFY_PIN_DIRECT]
			}
			return null
		}

	@Throws(SCIOException::class, InterruptedException::class)
	fun supportsPinCompare(): Boolean = this.pinCompareCtrlCode != null

	companion object {
		fun buildPACEProtocolList(paceCapabilities: List<PACECapabilities.PACECapability>): List<String> {
			val supportedProtos = mutableListOf<String>()
			for (next in paceCapabilities) {
				supportedProtos.add(next.protocol)
			}
			return supportedProtos
		}
	}
}
