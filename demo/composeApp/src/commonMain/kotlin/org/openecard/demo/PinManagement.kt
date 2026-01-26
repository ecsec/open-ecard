package org.openecard.demo

import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.bundled.NpaDefinitions.Apps.Mf
import org.openecard.cif.bundled.NpaDefinitions.Apps.Mf.Dids.paceCan
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.iface.dids.PinDid
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.SecurityCommandSuccess
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.withContextSuspend
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

@OptIn(ExperimentalUnsignedTypes::class)
fun PaceDid.enterPassword(pin: String): SecurityCommandFailure? {
	try {
		establishChannel(pin, null, null)
		return null
	} catch (ex: PaceError) {
		val secErr = ex.securityError
		if (secErr != null) {
			return secErr
		} else {
			throw ex
		}
	}
}

suspend fun changePassword(
	terminalFactory: TerminalFactory?,
	oldPin: String,
	newPin: String,
	nfcDetected: () -> Unit,
): PinStatus {
	return try {
		terminalFactory?.load()?.withContextSuspend { ctx ->
			val recognition =
				DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType)))

			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)

			val session = sal.startSession()

			when (val terminal = ctx.getTerminal("")) {
				null -> {
					throw Error("NO TERMINAL")
				}

				else -> {
					terminal.waitForCardPresent()
					nfcDetected()

					val con = session.connect(terminal.name)
					val mf = con.applications.find { it.name == Mf.name }
					mf?.connect()

					val pacePin: PaceDid =
						mf?.dids?.filterIsInstance<PaceDid>()?.find { it.name == "PACE_PIN" }
							?: throw IllegalStateException("PACE PIN not found")

					val pin: PinDid? = mf.dids.filterIsInstance<PinDid>().find { it.name == "PIN" }

					when (val pinStatus = pacePin.passwordStatus()) {
						is SecurityCommandSuccess -> {
							val result = pacePin.enterPassword(oldPin)
							return@withContextSuspend if (result == null) {
								pin?.resetPassword(null, newPin)
								pacePin.closeChannel()
								PinStatus.OK
							} else {
								PinStatus.Unknown
							}
						}

						is SecurityCommandFailure -> {
							when (pinStatus.retries) {
								3 -> PinStatus.OK
								2 -> PinStatus.OK
								1 -> PinStatus.Suspended
								0 -> PinStatus.Blocked
								else -> PinStatus.Unknown
							}
// 							PinStatus.Unknown
						}
					}
// 					pacePin.closeChannel()
// 					PinStatus.Unknown
				}
			}
		} ?: PinStatus.Unknown
	} catch (e: Throwable) {
		"horrible error ${e.message}"
		PinStatus.Unknown
	}
}

suspend fun suspendRecovery(
	terminalFactory: TerminalFactory?,
	can: String,
	pin: String,
): PinStatus {
	return try {
		terminalFactory?.load()?.withContextSuspend { ctx ->
			val recognition =
				DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType)))
			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)

			val session = sal.startSession()

			when (val terminal = ctx.getTerminal("")) {
				null -> {
					throw Error("NO TERMINAL")
				}

				else -> {
					terminal.waitForCardPresent()

					val con = session.connect(terminal.name)
					val mf = con.applications.find { it.name == Mf.name }
					mf?.connect()

					val pacePin: PaceDid =
						mf?.dids?.filterIsInstance<PaceDid>()?.find { it.name == "PACE_PIN" }
							?: throw IllegalStateException("PACE PIN not found")

					val paceCan = mf.dids.filterIsInstance<PaceDid>().find { it.name == paceCan }

					when (val pinStatus = pacePin.passwordStatus()) {
						is SecurityCommandSuccess -> {
							PinStatus.OK
						}

						is SecurityCommandFailure -> {
							when (pinStatus.retries) {
								3 -> {
									PinStatus.OK
								}

								2 -> {
									PinStatus.OK
								}

								1 -> {
									val result = paceCan?.enterPassword(can)
									return@withContextSuspend if (result == null) {
										pacePin.enterPassword(pin)
										paceCan?.closeChannel()
										PinStatus.OK
									} else {
										PinStatus.Unknown
									}
								}

								0 -> {
									PinStatus.Blocked
								}

								else -> {
									PinStatus.Unknown
								}
							}
						}
					}
				}
			}
		} ?: PinStatus.Unknown
	} catch (e: Exception) {
		PinStatus.Unknown
	}
}

suspend fun unblockPin(
	terminalFactory: TerminalFactory?,
	puk: String,
): PinStatus {
	return try {
		terminalFactory?.load()?.withContextSuspend { ctx ->
			val recognition =
				DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType)))
			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)

			val session = sal.startSession()

			when (val terminal = ctx.getTerminal("")) {
				null -> {
					throw Error("NO TERMINAL")
				}

				else -> {
					terminal.waitForCardPresent()

					val con = session.connect(terminal.name)
					val mf = con.applications.find { it.name == Mf.name }
					mf?.connect()

					val pacePin: PaceDid =
						mf?.dids?.filterIsInstance<PaceDid>()?.find { it.name == "PACE_PIN" }
							?: throw IllegalStateException("PACE PIN not found")

					val pacePuk = mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.pacePuk }

					when (val pinStatus = pacePin.passwordStatus()) {
						is SecurityCommandSuccess -> {
							PinStatus.OK
						}

						is SecurityCommandFailure -> {
							when (pinStatus.retries) {
								3 -> {
									PinStatus.OK
								}

								2 -> {
									PinStatus.OK
								}

								1 -> {
									PinStatus.Suspended
								}

								0 -> {
									val result = pacePuk?.enterPassword(puk)
									return@withContextSuspend if (result == null) {
										pacePuk?.closeChannel()
										PinStatus.OK
									} else {
										PinStatus.Unknown
									}
								}

								else -> {
									PinStatus.Unknown
								}
							}
						}
					}
				}
			}
		} ?: PinStatus.Unknown
	} catch (e: Exception) {
		PinStatus.Unknown
	}
}

suspend fun suspendRecovery2(
	terminalFactory: TerminalFactory?,
	can: String,
	pin: String,
): PinStatus {
	return try {
		terminalFactory?.load()?.withContextSuspend { ctx ->
			val recognition =
				DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType)))
			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)

			val session = sal.startSession()

			when (val terminal = ctx.getTerminal("")) {
				null -> {
					throw Error("NO TERMINAL")
				}

				else -> {
					terminal.waitForCardPresent()

					val con = session.connect(terminal.name)
					val mf = con.applications.find { it.name == Mf.name }
					mf?.connect()

					val pacePin = mf?.dids?.filterIsInstance<PaceDid>()?.find { it.name == Mf.Dids.pacePin }
					val paceCan = mf?.dids?.filterIsInstance<PaceDid>()?.find { it.name == Mf.Dids.paceCan }

					when (val pinStatus = pacePin?.passwordStatus()) {
						is SecurityCommandSuccess -> {
							PinStatus.OK
						}

						is SecurityCommandFailure -> {
							when (pinStatus.retries) {
								3 -> {
									PinStatus.OK
								}

								2 -> {
									PinStatus.OK
								}

								1 -> {
									val result = paceCan?.enterPassword(can)
									return@withContextSuspend if (result == null) {
										pacePin.enterPassword(pin)
										PinStatus.OK
									} else {
										PinStatus.Unknown
									}
								}

								0 -> {
									PinStatus.Blocked
								}

								else -> {
									PinStatus.Unknown
								}
							}
						}

						else -> {
							PinStatus.Unknown
						}
					}
// 					paceCan?.closeChannel()
				}
			}

// 			PinStatus.Unknown
		} ?: PinStatus.Unknown
	} catch (e: Exception) {
		PinStatus.Unknown
	}
}

// fun changePassword2(
// 	terminalFactory: TerminalFactory?,
// 	oldPin: String,
// 	newPin: String,
// // 	can: String?,
// // 	puk: String?,
// ): Boolean {
// 	return try {
// 		terminalFactory?.load()?.withContext { ctx ->
// 			val recognition =
// 				DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType)))
//
// 			val paceFactory = PaceFeatureSoftwareFactory()
// 			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)
//
// 			val session = sal.startSession()
//
// 			when (val terminal = ctx.getTerminal("")) {
// 				null -> {
// 					throw Error("NO TERMINAL")
// 				}
//
// 				else -> {
// // 						terminal.waitForCardPresent()
//
// 					val con = session.connect(terminal.name)
// 					val mf = con.applications.find { it.name == Mf.name }
// 					mf?.connect()
//
// // 					val pacePin = mf?.dids?.filterIsInstance<PaceDid>()?.find { it.name == pacePin }
// // 					val pin = mf?.dids?.filterIsInstance<PinDid>()?.find { it.name == pin }
//
// 					val pacePin: PaceDid =
// 						mf?.dids?.filterIsInstance<PaceDid>()?.find { it.name == "PACE_PIN" }
// 							?: throw IllegalStateException("PACE PIN not found")
//
// 					val paceCan: PaceDid? = mf.dids.filterIsInstance<PaceDid>().find { it.name == "PACE_CAN" }
// 					val pacePuk: PaceDid? = mf.dids.filterIsInstance<PaceDid>().find { it.name == "PACE_PUK" }
// 					val pin: PinDid? = mf.dids.filterIsInstance<PinDid>().find { it.name == "PIN" }
//
// 					when (val pinStatus = pacePin.passwordStatus()) {
// 						is SecurityCommandFailure -> {
// // 							throw Error("PIN must be in RC3 state to continue")
//
// 							when (pinStatus.retries) {
// 								1 -> {
// 									suspendRecovery(terminalFactory, null, null)
// 								}
//
// 								0 -> {
// 								}
// 							}
// 							false
// 						}
//
// 						is SecurityCommandSuccess -> {
// 							val result = pacePin.enterPassword(oldPin)
// 							return@withContext if (result == null) {
// 								pin?.resetPassword(null, newPin)
// 								pacePin.closeChannel()
// 								true
// 							} else {
// 								false
// 							}
// 						}
// 					}
// // 					pin?.resetPassword(null, "123456")
// 				}
// 			}
// 		} ?: false
// 	} catch (e: Throwable) {
// 		"horrible error ${e.message}"
// 		false
// 	}
// }
//
