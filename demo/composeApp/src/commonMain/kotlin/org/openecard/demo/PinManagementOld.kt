package org.openecard.demo

import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.bundled.NpaDefinitions.Apps.Mf
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.iface.dids.PinDid
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.SecurityCommandSuccess
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.withContext
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

// package pinmanagement
//
// import org.openecard.cif.bundled.CompleteTree
// import org.openecard.cif.bundled.NpaCif
// import org.openecard.cif.bundled.NpaDefinitions.Apps.Mf
// import org.openecard.sal.iface.dids.PaceDid
// import org.openecard.sal.iface.dids.PinDid
// import org.openecard.sal.sc.SmartcardSal
// import org.openecard.sal.sc.recognition.DirectCardRecognition
// import org.openecard.sc.apdu.command.SecurityCommandFailure
// import org.openecard.sc.apdu.command.SecurityCommandSuccess
// import org.openecard.sc.iface.feature.PaceError
// import org.openecard.sc.iface.withContext
// import org.openecard.sc.pace.PaceFeatureSoftwareFactory
// import org.openecard.sc.pcsc.PcscTerminalFactory
//
// class PINDialog(
// 	val npaPin: String,
// 	val npaCan: String,
// 	val npaPuk: String,
// ) {
// 	@OptIn(ExperimentalUnsignedTypes::class)
// 	fun PaceDid.enterPassword(pin: String): SecurityCommandFailure? {
// 		try {
// 			establishChannel(pin, null, null)
// 			return null
// 		} catch (ex: PaceError) {
// 			val secErr = ex.securityError
// 			if (secErr != null) {
// 				return secErr
// 			} else {
// 				throw ex
// 			}
// 		}
// 	}
//
// 	@OptIn(ExperimentalUnsignedTypes::class)
// 	fun changePassword() {
// 		PcscTerminalFactory.instance.load().withContext { ctx ->
// 			val terminal =
// 				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
// 					?: throw IllegalStateException("Necessary terminal not available")
// 			if (!terminal.isCardPresent()) throw IllegalStateException("Terminal does not contain a card")
//
// 			val recognition = DirectCardRecognition(CompleteTree.calls)
// 			val paceFactory = PaceFeatureSoftwareFactory()
// 			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)
//
// 			val session = sal.startSession()
// 			val con = session.connect(terminal.name)
// 			if (NpaCif.metadata.id != con.cardType) throw IllegalStateException("Recognized card is not an nPA")
//
// 			val mf =
// 				con.applications.find { it.name == Mf.name } ?: throw IllegalStateException("MF application not found")
// 			mf.connect()
//
// 			val pacePin =
// 				mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.pacePin }
// 					?: throw IllegalStateException("PACE PIN not found")
//
// 			val pinStatus = pacePin.passwordStatus()
// 			if (pinStatus is SecurityCommandFailure) {
// 				throw IllegalStateException("PIN must be in RC3 state to continue")
// 			}
//
// 			pacePin.enterPassword(npaPin)
// 		}
// 	}
//
// 	@OptIn(ExperimentalUnsignedTypes::class)
// 	fun unblockPassword() {
// 		PcscTerminalFactory.instance.load().withContext { ctx ->
// 			val terminal =
// 				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
// 					?: throw IllegalStateException("Necessary terminal not available")
// 			if (!terminal.isCardPresent()) throw IllegalStateException("Terminal does not contain a card")
//
// 			val recognition = DirectCardRecognition(CompleteTree.calls)
// 			val paceFactory = PaceFeatureSoftwareFactory()
// 			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)
//
// 			val session = sal.startSession()
// 			val con = session.connect(terminal.name)
// 			if (NpaCif.metadata.id != con.cardType) throw IllegalStateException("Recognized card is not an nPA")
//
// 			val mf =
// 				con.applications.find { it.name == Mf.name } ?: throw IllegalStateException("MF application not found")
// 			mf.connect()
//
// 			val pacePin =
// 				mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.pacePin }
// 					?: throw IllegalStateException("PACE PIN not found")
// 			val paceCan =
// 				mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.paceCan }
// 					?: throw IllegalStateException("PACE CAN not found")
// 			val pacePuk =
// 				mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.pacePuk }
// 					?: throw IllegalStateException("PACE PUK not found")
// 			val pin =
// 				mf.dids.filterIsInstance<PinDid>().find { it.name == Mf.Dids.pin }
// 					?: throw IllegalStateException("PIN DID not found")
//
// 			when (val pinStatus = pacePin.passwordStatus()) {
// 				is SecurityCommandFailure -> {
// 					when (pinStatus.retries) {
// 						2 -> {
// 							val result = pacePin.enterPassword(npaPin)
// 							if (result == null || !result.verificationFailed || result.retries != 1) {
// 								throw IllegalStateException("Unexpected result after wrong PIN entry")
// 							}
// 						}
//
// 						1 -> {
// 							paceCan.enterPassword(npaCan)
// 							val result = pacePin.enterPassword(npaPin)
// 							if (result == null || !result.authBlocked) {
// 								throw IllegalStateException("PIN should be blocked")
// 							}
// 						}
// 					}
// 				}
//
// 				is SecurityCommandSuccess -> {
// 					repeat(2) {
// 						val result = pacePin.enterPassword(npaPin)
// 						if (result == null || !result.verificationFailed) {
// 							throw IllegalStateException("PIN verification should fail")
// 						}
// 					}
// 					paceCan.enterPassword(npaCan)
// 					val result = pacePin.enterPassword(npaPin)
// 					if (result == null || !result.authBlocked) {
// 						throw IllegalStateException("PIN should be blocked")
// 					}
// 				}
// 			}
//
// 			paceCan.closeChannel()
// 			pacePuk.enterPassword(npaPuk)
// 			pin.resetPassword(null, null)
// 			pacePuk.closeChannel()
//
// 			val pinStatus2 = pacePin.passwordStatus()
// 			if (pinStatus2 is SecurityCommandFailure) {
// 				throw IllegalStateException("PIN status reports error after successful reset with PUK")
// 			}
//
// 			pacePin.enterPassword(npaPin)
// 		}
// 	}
// }
