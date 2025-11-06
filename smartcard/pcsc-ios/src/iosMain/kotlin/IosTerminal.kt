
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.openecard.sc.iface.CancelledByUser
import org.openecard.sc.iface.CommError
import org.openecard.sc.iface.InternalSystemError
import org.openecard.sc.iface.NoService
import org.openecard.sc.iface.NoSmartcard
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.SmartcardException
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalStateType
import org.openecard.sc.iface.Terminals
import org.openecard.sc.iface.Timeout
import platform.CoreNFC.NFCISO7816TagProtocol
import platform.CoreNFC.NFCPollingISO14443
import platform.CoreNFC.NFCTagReaderSession
import platform.CoreNFC.NFCTagReaderSessionDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

private val logger = KotlinLogging.logger { }

internal class IosNfcTagReaderSession(
	private val terminal: IosTerminal,
) {
	private var sessionDidActivate: CompletableDeferred<Nothing?>? = null
	private var sessionDidDeactivate: CompletableDeferred<Nothing?>? = null

	lateinit var deferredTag: CompletableDeferred<NFCISO7816TagProtocol>

	@OptIn(ExperimentalCoroutinesApi::class)
	val tag: NFCISO7816TagProtocol
		get() =
			if (deferredTag.isCompleted) {
				deferredTag.getCompleted()
			} else {
				throw NoSmartcard()
			}

	fun setAlertMessage(msg: String) {
		nfcReaderSession.alertMessage = msg
	}

	var active: Boolean = false

	private val nfcTagReaderSessionDelegate: NFCTagReaderSessionDelegateProtocol =
		object :
			NSObject(),
			NFCTagReaderSessionDelegateProtocol {
			override fun tagReaderSession(
				session: NFCTagReaderSession,
				didInvalidateWithError: NSError,
			) {
				logger.debug { "tagReaderSession didInvalidateWithError: $didInvalidateWithError" }

				// this can be called before session gets active we cancel then
				sessionDidActivate?.cancel(
					CancellationException(
						when (didInvalidateWithError.code) {
							202L -> NoService("Nfc could not be started: ${didInvalidateWithError.localizedDescription}")
							else -> null
						},
					),
				)

				// didinvalidate is no result of calling "deactivate" (user hit abort or sth. else)
				if (sessionDidDeactivate == null) {
					nfcReaderSession.alertMessage = IosNfcAlertMessages.nfcErrorMessage
				}
				deferredTag.cancel(
					CancellationException(
						when (didInvalidateWithError.code) {
							200L -> CancelledByUser()
							201L -> Timeout("iOS NFC discovery timeout reached")
							else -> null
						},
					),
				)
				active = false
				terminal.currentSession = null
				sessionDidDeactivate?.complete(null)
			}

			@OptIn(ExperimentalForeignApi::class)
			override fun tagReaderSession(
				session: NFCTagReaderSession,
				didDetectTags: List<*>,
			) {
				(didDetectTags.firstOrNull() as? NFCISO7816TagProtocol)?.let {
					nfcReaderSession.connectToTag(it) { er ->
						er?.let {
							throw CommError(er.localizedDescription)
						}
					}
					nfcReaderSession.alertMessage = IosNfcAlertMessages.cardInsertedMessage
					deferredTag.complete(it)
				} ?: {
					// this does most probably not happen, since polling option searches for iso7816 only
					nfcReaderSession.alertMessage = IosNfcAlertMessages.cardNotSupported
				}
			}

			override fun tagReaderSessionDidBecomeActive(session: NFCTagReaderSession) {
				nfcReaderSession.alertMessage = IosNfcAlertMessages.provideCardMessage
				logger.debug { "Session did become active." }
				active = true
				sessionDidActivate?.complete(null)
			}
		}
	private val nfcReaderSession =
		NFCTagReaderSession(
			NFCPollingISO14443,
			nfcTagReaderSessionDelegate,
			null,
		)

	internal fun activate() {
		sessionDidActivate = CompletableDeferred()
		deferredTag = CompletableDeferred()
		nfcReaderSession.beginSession()
		try {
			runBlocking {
				logger.debug { "Waiting for session to become active." }
				sessionDidActivate?.await()
				sessionDidActivate = null
			}
		} catch (e: CancellationException) {
			logger.error { "Waiting for session to become active was cancelled $e" }
			when (val c = e.cause) {
				is SmartcardException -> {
					throw c
				}
				else -> throw InternalSystemError(cause = e.cause)
			}
		}
	}

	internal fun deActivate() {
		sessionDidDeactivate = CompletableDeferred()
		nfcReaderSession.invalidateSession()
		try {
			runBlocking {
				logger.debug { "Waiting for session to become invalidated." }
				sessionDidDeactivate?.await()
				sessionDidDeactivate = null
			}
		} catch (e: CancellationException) {
			logger.error { "Waiting for session to become invalidated was cancelled $e" }
			when (val c = e.cause) {
				is SmartcardException -> {
					throw c
				}
				else -> throw InternalSystemError(cause = e.cause)
			}
		}
	}
}

class IosTerminal(
	override val terminals: Terminals,
	override val name: String,
) : Terminal {
	val iosNfcAlertMessages = IosNfcAlertMessages
	internal var currentSession: IosNfcTagReaderSession? = null

	fun activate() {
		if (currentSession == null) {
			logger.debug { "Activating session." }
			currentSession = IosNfcTagReaderSession(this).apply { activate() }
		} else {
			logger.warn { "Session already active." }
		}
	}

	fun deActivate() {
		if (currentSession != null) {
			logger.debug { "Deactivating session." }
			currentSession?.setAlertMessage(IosNfcAlertMessages.nfcCompletionMessage)
			currentSession?.deActivate()
		} else {
			logger.warn { "No session to deactivate." }
		}
	}

	val sessionActive: Boolean
		get() = currentSession?.active ?: false

	override fun isCardPresent() = getState() == TerminalStateType.PRESENT

	@OptIn(ExperimentalCoroutinesApi::class)
	override fun getState(): TerminalStateType =
		try {
			if (currentSession?.tag?.isAvailable() == true) {
				TerminalStateType.PRESENT
			} else {
				TerminalStateType.ABSENT
			}
		} catch (e: NoSmartcard) {
			TerminalStateType.ABSENT
		}

	override fun connectTerminalOnly() = IosTerminalConnection(this)

	override fun connect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	) = if (isCardPresent()) {
		connectTerminalOnly()
	} else {
		throw NoSmartcard()
	}

	override suspend fun waitForCardPresent() {
		currentSession?.deferredTag?.await()
	}

	override suspend fun waitForCardAbsent() {
		TODO("Not yet implemented")
	}

	fun setAlertMessage(msg: String) {
		currentSession?.setAlertMessage(msg)
	}
}
