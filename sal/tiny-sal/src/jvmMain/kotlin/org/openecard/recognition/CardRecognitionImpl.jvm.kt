/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.recognition

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.BeginTransaction
import iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse
import iso.std.iso_iec._24727.tech.schema.CardCall
import iso.std.iso_iec._24727.tech.schema.CardInfoType
import iso.std.iso_iec._24727.tech.schema.Connect
import iso.std.iso_iec._24727.tech.schema.ConnectResponse
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DataMaskType
import iso.std.iso_iec._24727.tech.schema.Disconnect
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse
import iso.std.iso_iec._24727.tech.schema.EndTransaction
import iso.std.iso_iec._24727.tech.schema.EndTransactionResponse
import iso.std.iso_iec._24727.tech.schema.GetCardInfoOrACDResponse
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType
import iso.std.iso_iec._24727.tech.schema.MatchingDataType
import iso.std.iso_iec._24727.tech.schema.RecognitionTree
import iso.std.iso_iec._24727.tech.schema.ResponseAPDUType
import iso.std.iso_iec._24727.tech.schema.Transmit
import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.common.AppVersion.name
import org.openecard.common.ECardConstants
import org.openecard.common.I18n
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.minorIsOneOf
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.interfaces.CardRecognition
import org.openecard.common.interfaces.Environment
import org.openecard.common.interfaces.RecognitionException
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.FileUtils.readLinesFromConfig
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.common.util.IntegerUtils
import org.openecard.gui.message.DialogType
import org.openecard.recognition.RecognitionProperties.action
import org.openecard.recognition.staticrepo.LocalCifRepo
import org.openecard.recognition.statictree.LocalFileTree
import org.openecard.ws.GetCardInfoOrACD
import org.openecard.ws.GetRecognitionTree
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.util.Locale
import java.util.Properties
import kotlin.concurrent.thread

private val LOG = KotlinLogging.logger { }

/**
 * Interface to use the card recognition.
 * This implementation provides card recognition based on a static tree.
 *
 * @author Tobias Wich
 */
class CardRecognitionImpl
	@JvmOverloads
	constructor(
		private val env: Environment,
		treeRepo: GetRecognitionTree? = null,
		cifRepo: GetCardInfoOrACD? = null,
	) : CardRecognition {
		private val tree: RecognitionTree by lazy {
			LOG.info { "Initializing RecognitionTree Repo." }
			val treeMarshaller = createInstance()
			var treeRepoTmp = treeRepo
			if (treeRepoTmp == null) {
				treeRepoTmp = LocalFileTree(treeMarshaller)
			}
			// request tree from service
			val req =
				iso.std.iso_iec._24727.tech.schema
					.GetRecognitionTree()
			req.setAction(action)
			val resp = treeRepoTmp.getRecognitionTree(req)
			checkResult(resp.getResult())

			LOG.info { "Finished initializing RecognitionTree Repo." }
			resp.getRecognitionTree()
		}

		private val cifRepo: GetCardInfoOrACD by lazy {
			LOG.info { "Initializing CIF Repo." }
			val cifMarshaller = createInstance()
			var cifRepoTmp = cifRepo
			if (cifRepoTmp == null) {
				cifRepoTmp = LocalCifRepo(cifMarshaller)
			}
			LOG.debug { "Done loading CIF documents." }

			cifRepoTmp
		}

		private val supportedCards: Set<String> by lazy {
			if (this.cifRepo is LocalCifRepo) {
				(this.cifRepo as LocalCifRepo).supportedCardTypes
			} else {
				try {
					try {
						val `in` = resolveResourceAsStream(javaClass, "cif-repo/supported_cards")
						var oids: List<String> =
							if (`in` == null) {
								throw IOException("File with supported cards not found.")
							} else {
								readLinesFromConfig(`in`, "UTF-8")
							}

						if (oids.size == 1 && oids[0] == "*") {
							oids = getAllTypesFromRepo(this.cifRepo)
						}

						oids.toSet()
					} catch (ex: IOException) {
						// no file loaded falling back to using everything from the repo
						val oids = getAllTypesFromRepo(this.cifRepo)
						oids.toSet()
					}
				} catch (ex: WSHelper.WSException) {
					error { "Failed to retrieve CIFs from repo, don't support any card." }
					setOf<String>()
				}
			}
		}

		private val cardImagesMap = Properties()

		/**
		 * Create recognizer with tree from local (file based) repository.
		 *
		 * @param env
		 * @throws Exception
		 */
		init {
			cardImagesMap.load(resolveResourceAsStream(CardRecognitionImpl::class.java, IMAGE_PROPERTIES))

			thread {
				tree
				LOG.debug { "Done determining loading tree." }
				// request all cifs to fill list of supported cards
				supportedCards
				LOG.debug { "Done determining supported cards." }

				LOG.info { "Finished initializing CIF Repo." }
			}
		}

		@Throws(WSHelper.WSException::class)
		private fun getAllTypesFromRepo(repo: GetCardInfoOrACD): List<String> {
			// read list of all cifs from the repo
			val req =
				iso.std.iso_iec._24727.tech.schema
					.GetCardInfoOrACD()
			req.setAction(ECardConstants.CIF.GET_OTHER)
			val res = repo.getCardInfoOrACD(req)
			checkResult<GetCardInfoOrACDResponse>(res)

			val oids = ArrayList<String>()
			for (cif in res.getCardInfoOrCapabilityInfo()) {
				if (cif is CardInfoType) {
					val type = cif.getCardType().getObjectIdentifier()
					oids.add(type)
				}
			}

			return oids
		}

		override val cardInfos: List<CardInfoType>
			get() {
				// TODO: add caching
				val req =
					iso.std.iso_iec._24727.tech.schema
						.GetCardInfoOrACD()
				req.setAction(ECardConstants.CIF.GET_OTHER)
				val res = cifRepo.getCardInfoOrACD(req)
				// checkout response if it contains our cardinfo
				val cifs = res.getCardInfoOrCapabilityInfo()
				val result = ArrayList<CardInfoType>()
				for (next in cifs) {
					if (next is CardInfoType) {
						result.add(next)
					}
				}
				return result
			}

		override fun getCardInfo(type: String): CardInfoType? {
			var cif = env.cifProvider?.getCardInfo(type)
			if (cif == null) {
				cif = getCardInfoFromRepo(type)
			}
			return cif
		}

		override fun getCardInfoFromRepo(type: String): CardInfoType? {
			val cif: CardInfoType? = null
			// only do something when a repo is specified

			val req =
				iso.std.iso_iec._24727.tech.schema
					.GetCardInfoOrACD()
			req.setAction(ECardConstants.CIF.GET_SPECIFIED)
			req.getCardTypeIdentifier().add(type)
			val res = cifRepo.getCardInfoOrACD(req)
			// checkout response if it contains our cardinfo
			val cifs = res.getCardInfoOrCapabilityInfo()
			for (next in cifs) {
				if (next is CardInfoType) {
					return next
				}
			}

			return cif
		}

		private fun isSupportedCard(type: String): Boolean = supportedCards.contains(type)

		/**
		 * Gets the translated card name for a card type.
		 *
		 * @param cardType The card type to get the card name for.
		 * @return A card name matching the users locale or the English name as default. If the card is not supported, the
		 * string `Unknown card type` is returned.
		 */
		override fun getTranslatedCardName(cardType: String): String {
			val info = getCardInfo(cardType)

			val userLocale = Locale.getDefault()
			val langCode = userLocale.language
			var enFallback = "Unknown card type."

			if (info == null) {
				// we can identify the card but do not have a card info file for it
				return enFallback
			}

			for (typ in info.getCardType().getCardTypeName()) {
				if (typ.getLang().equals("en", ignoreCase = true)) {
					enFallback = typ.getValue()
				}
				if (typ.getLang().equals(langCode, ignoreCase = true)) {
					return typ.getValue()
				}
			}
			return enFallback
		}

		/**
		 * Gets image stream of the given card or a no card image if the object identifier is unknown.
		 * @param objectid iso:ObjectIdentifier as defined in the CardInfo file.
		 * @return InputStream of the card image.
		 */
		override fun getCardImage(objectid: String): InputStream? {
			val fname = cardImagesMap.getProperty(objectid)
			// Get the card image as inputstream from the responsible Middleware SAL
			var fs = env.cifProvider?.getCardImage(objectid)
			// If null is returned, load the Image from the local cif repo
			if (fs == null && fname != null) {
				fs = loadCardImage(fname)
			}
			if (fs == null) {
				fs = unknownCardImage
			}
			return fs
		}

		/**
		 * @return Stream containing the requested image.
		 * @see .getCardImage
		 */
		override val unknownCardImage: InputStream
			get() {
				return loadCardImage("unknown_card.png")!!
			}

		/**
		 * @return Stream containing the requested image.
		 * @see .getCardImage
		 */
		override val noCardImage: InputStream
			get() {
				return loadCardImage("no_card.jpg")!!
			}

		/**
		 * @return Stream containing the requested image.
		 * @see .getCardImage
		 */
		override val noTerminalImage: InputStream
			get() {
				return loadCardImage("no_terminal.png")!!
			}

		/**
		 * Recognizes the card in the defined reader.
		 *
		 * @param ctx Context handle of the IFD.
		 * @param ifdName Name of the card reader.
		 * @param slot Index of the slot in the reader.
		 * @return RecognitionInfo structure containing the card type of the detected card or `null` if no card could
		 * be detected.
		 * @throws RecognitionException Thrown in case there was an error in the recognition.
		 */
		@Throws(RecognitionException::class)
		override fun recognizeCard(
			ctx: ByteArray,
			ifdName: String,
			slot: BigInteger,
		): ConnectionHandleType.RecognitionInfo? {
			// connect card
			val slotHandle = connect(ctx, ifdName, slot)
			try {
				// recognise card
				val type = treeCalls(slotHandle, tree.getCardCall())
				// build result or throw exception if it is null or unsupported
				if (type == null || !isSupportedCard(type)) {
					return null
				}
				val info = ConnectionHandleType.RecognitionInfo()
				info.setCardType(type)
				return info
			} finally {
				disconnect(slotHandle)
			}
		}

		@Throws(RecognitionException::class)
		private fun checkResult(r: Result) {
			if (r.getResultMajor() == ECardConstants.Major.ERROR) {
				throw RecognitionException(r)
			}
		}

		/**
		 * Special transmit check determining only whether a response is present or not and it contains at least a trailer.
		 * Unexpected result may be the wrong cause, because the command could represent multiple commands.
		 *
		 * @param r The response to check
		 * @return True when result present, false otherwise.
		 * @throws RecognitionException Thrown in case there is a critical error returned from the IFD which is preventing
		 * continuous options.
		 */
		@Throws(RecognitionException::class)
		private fun checkTransmitResult(r: TransmitResponse): Boolean {
			// check if card has been removed
			if (minorIsOneOf<TransmitResponse>(r, ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)) {
				val msg = "The card is not accessible anymore to the IFD layer."
				throw RecognitionException(r.getResult().getResultMinor(), msg)
			}

			if (!r.getOutputAPDU().isEmpty() && r.getOutputAPDU().get(0).size >= 2) {
				return true
			} else {
				return false
			}
		}

		/**
		 * Returns the fibonacci number for a given index.
		 *
		 * @param idx index
		 * @return the fibonacci number for the given index
		 */
		private fun fibonacci(idx: Int): Long {
			if (idx == 1 || idx == 2) {
				return 1
			} else {
				return fibonacci(idx - 1) + fibonacci(idx - 2)
			}
		}

		@Throws(RecognitionException::class)
		private fun connect(
			ctx: ByteArray,
			ifdName: String,
			slot: BigInteger,
		): ByteArray {
			val c = Connect()
			c.setContextHandle(ctx)
			c.setIFDName(ifdName)
			c.setSlot(slot)

			val r = env.dispatcher!!.safeDeliver(c) as ConnectResponse
			checkResult(r.getResult())

			waitForExclusiveCardAccess(r.getSlotHandle(), ifdName)

			return r.getSlotHandle()
		}

		/**
		 * This method tries to get exclusive card access until it is granted.
		 * The waiting delay between the attempts is determined by the fibonacci numbers.
		 *
		 * @param slotHandle slot handle specifying the card to get exclusive access for
		 * @param ifdName Name of the IFD in which the card is inserted
		 */
		@Throws(RecognitionException::class)
		private fun waitForExclusiveCardAccess(
			slotHandle: ByteArray,
			ifdName: String,
		) {
			var resultMajor: String
			var i = 2
			do {
				// try to get exclusive card access for the recognition run
				val trans = BeginTransaction()
				trans.setSlotHandle(slotHandle)
				val resp = env.dispatcher!!.safeDeliver(trans) as BeginTransactionResponse
				resultMajor = resp.getResult().getResultMajor()

				if (resultMajor != ECardConstants.Major.OK) {
					val resultMinor = resp.getResult().getResultMinor()
					if (ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE == resultMinor) {
						throw RecognitionException("Card is not available anymore.")
					}
					// could not get exclusive card access, wait in increasingly longer intervals and retry
					try {
						val waitInSeconds = fibonacci(i)
						i++
						LOG.debug { "Could not get exclusive card access. Trying again in $waitInSeconds seconds." }
						if (i == 6 && env.gui != null) {
							val dialog = env.gui!!.obtainMessageDialog()
							val message = LANG.translationForKey("message", name, ifdName)
							val title = LANG.translationForKey("error", ifdName)
							dialog.showMessageDialog(message, title, DialogType.WARNING_MESSAGE)
						}
						Thread.sleep(1000 * waitInSeconds)
					} catch (e: InterruptedException) {
						// ignore
					}
				}
			} while (resultMajor != ECardConstants.Major.OK)
		}

		@Throws(RecognitionException::class)
		private fun disconnect(slotHandle: ByteArray) {
			// end exclusive card access
			val end = EndTransaction()
			end.setSlotHandle(slotHandle)
			val endTransactionResponse = env.dispatcher!!.safeDeliver(end) as EndTransactionResponse
			checkResult(endTransactionResponse.getResult())

			val d = Disconnect()
			d.setSlotHandle(slotHandle)
			val r = env.dispatcher!!.safeDeliver(d) as DisconnectResponse
			checkResult(r.getResult())
		}

		@Throws(RecognitionException::class)
		private fun transmit(
			slotHandle: ByteArray,
			input: ByteArray,
			results: List<ResponseAPDUType>,
		): ByteArray? {
			val t = Transmit()
			t.setSlotHandle(slotHandle)
			val apdu = InputAPDUInfoType()
			apdu.setInputAPDU(input)
			for (result in results) {
				apdu.getAcceptableStatusCode().add(result.getTrailer())
			}
			t.getInputAPDUInfo().add(apdu)

			val r = env.dispatcher!!.safeDeliver(t) as TransmitResponse
			return if (checkTransmitResult(r)) {
				r.getOutputAPDU()[0]
			} else {
				null
			}
		}

		private fun branch2list(first: CardCall): List<CardCall> {
			val calls = mutableListOf<CardCall>()
			calls.add(first)

			var next = first
			// while next is a select call
			while (next.getResponseAPDU()[0].getBody() == null) {
				// a select only has one call in its conclusion
				next = next.getResponseAPDU()[0].getConclusion().getCardCall()[0]
				calls.add(next)
			}

			return calls
		}

		@Throws(RecognitionException::class)
		private fun treeCalls(
			slotHandle: ByteArray,
			calls: List<CardCall>,
		): String? {
			for (c in calls) {
				// make list of next feature (aka branch)
				val branch = branch2list(c)
				// execute selects and then matcher, matcher decides over success
				for (next in branch) {
					val matcher = if (next.getResponseAPDU()[0].getBody() != null) true else false
					val resultBytes = transmit(slotHandle, next.getCommandAPDU(), next.getResponseAPDU())
					// break when outcome is wrong
					if (resultBytes == null) {
						break
					}
					// get command bytes and trailer
					val result = CardResponseAPDU.getData(resultBytes)
					val trailer = CardResponseAPDU.getTrailer(resultBytes)
					// if select, only one response exists
					if (!matcher && !next.getResponseAPDU()[0].getTrailer().contentEquals(trailer)) {
						// break when outcome is wrong
						break
					} else if (!matcher) {
						// trailer matches expected response from select, continue
						continue
					} else {
						// matcher command, loop through responses
						for (r in next.getResponseAPDU()) {
							// next response, when outcome is wrong
							if (!r.getTrailer().contentEquals(trailer)) {
								continue
							}
							// check internals for match
							if (checkDataObject(r.getBody(), result)) {
								if (r.getConclusion().getRecognizedCardType() != null) {
									// type recognised
									return r.getConclusion().getRecognizedCardType()
								} else {
									// type dependent on subtree
									return treeCalls(slotHandle, r.getConclusion().getCardCall())
								}
							}
						}
					}
				}
			}

			return null
		}

		private fun checkDataObject(
			matcher: DataMaskType,
			result: ByteArray,
		): Boolean {
			if (matcher.getTag() != null) {
				try {
					val tlv = TLV.fromBER(result)
					val tagNum = ByteUtils.toLong(matcher.getTag())
					val nextTlvs = tlv.findNextTags(tagNum)
					for (next in nextTlvs) {
						if (matcher.getDataObject() != null) {
							val outcome = checkDataObject(matcher.getDataObject(), next.value)
							if (outcome) {
								return true
							}
						} else if (matcher.getMatchingData() != null) {
							val outcome = checkMatchingData(matcher.getMatchingData(), next.value)
							if (outcome) {
								return true
							}
						} else {
							error { "No data object or matching data found in DataMaskType." }
							return false
						}
					}
				} catch (ex: TLVException) {
					// no TLV, hence no tag to match
					return false
				}
			}

			if (matcher.getDataObject() != null) {
				return checkDataObject(matcher.getDataObject(), result)
			} else if (matcher.getMatchingData() != null) {
				return checkMatchingData(matcher.getMatchingData(), result)
			} else {
				error { "No data object or matching data found in DataMaskType." }
				return false
			}
		}

		private fun checkMatchingData(
			matcher: MatchingDataType,
			result: ByteArray,
		): Boolean {
			// get values
			var offsetBytes = matcher.getOffset()
			var lengthBytes = matcher.getLength()
			val valueBytes = matcher.getMatchingValue()
			var maskBytes = matcher.getMask()

			// convert values for convenience
			if (offsetBytes == null) {
				offsetBytes = byteArrayOf(0x00.toByte(), 0x00.toByte())
			}
			val offset = ByteUtils.toInteger(offsetBytes)
			if (lengthBytes == null) {
				lengthBytes = IntegerUtils.toByteArray(valueBytes.size)
			}
			val length = ByteUtils.toInteger(lengthBytes)
			if (maskBytes == null) {
				maskBytes = ByteArray(valueBytes.size)
				for (i in maskBytes.indices) {
					maskBytes[i] = 0xFF.toByte()
				}
			}

			// some basic integrity checks
			if (maskBytes.size != valueBytes.size) {
				return false
			}
			if (valueBytes.size != length) {
				return false
			}
			if (result.size < length + offset) {
				return false
			}

			// check
			for (i in offset..<length + offset) {
				if ((maskBytes[i - offset].toInt() and result[i].toInt()) != valueBytes[i - offset].toInt()) {
					return false
				}
			}

			return true
		}
	}

private val LANG: I18n = I18n.getTranslation("recognition")
private const val IMAGE_PROPERTIES = "/card-images/card-images.properties"

/**
 * Gets stream of the given image in the directory card-images.
 * @param filename
 * @return Stream of the image or null, if none is found.
 */
private fun loadCardImage(filename: String?): InputStream? {
	try {
		return resolveResourceAsStream(CardRecognitionImpl::class.java, "/card-images/$filename")
	} catch (ex: IOException) {
		LOG.info(ex) { "Failed to load card image '$filename'." }
		return null
	}
}
