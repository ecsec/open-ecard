package org.openecard.sc.pcsc

import au.id.micolous.kotlin.pcsc.PCSCError
import au.id.micolous.kotlin.pcsc.PCSCErrorCode
import org.openecard.sc.iface.BadSeek
import org.openecard.sc.iface.CacheItemNotFound
import org.openecard.sc.iface.CacheItemStale
import org.openecard.sc.iface.CacheItemTooBig
import org.openecard.sc.iface.Cancelled
import org.openecard.sc.iface.CancelledByUser
import org.openecard.sc.iface.CantDispose
import org.openecard.sc.iface.CardNotAuthenticated
import org.openecard.sc.iface.CardReadOnly
import org.openecard.sc.iface.CardUnsupported
import org.openecard.sc.iface.CertificateUnavailable
import org.openecard.sc.iface.ChvBlocked
import org.openecard.sc.iface.CommDataLost
import org.openecard.sc.iface.CommError
import org.openecard.sc.iface.DeviceNotReady
import org.openecard.sc.iface.DirNotFound
import org.openecard.sc.iface.DuplicateReader
import org.openecard.sc.iface.Eof
import org.openecard.sc.iface.FileNotFound
import org.openecard.sc.iface.IccCreateorder
import org.openecard.sc.iface.IccInstallation
import org.openecard.sc.iface.InsufficientBuffer
import org.openecard.sc.iface.InternalSystemError
import org.openecard.sc.iface.InvalidAtr
import org.openecard.sc.iface.InvalidChv
import org.openecard.sc.iface.InvalidHandle
import org.openecard.sc.iface.InvalidParameter
import org.openecard.sc.iface.InvalidTarget
import org.openecard.sc.iface.InvalidValue
import org.openecard.sc.iface.NoAccess
import org.openecard.sc.iface.NoDir
import org.openecard.sc.iface.NoFile
import org.openecard.sc.iface.NoKeyContainer
import org.openecard.sc.iface.NoMemory
import org.openecard.sc.iface.NoPinCache
import org.openecard.sc.iface.NoReadersAvailable
import org.openecard.sc.iface.NoService
import org.openecard.sc.iface.NoSmartcard
import org.openecard.sc.iface.NoSuchCertificate
import org.openecard.sc.iface.NotTransacted
import org.openecard.sc.iface.PciTooSmall
import org.openecard.sc.iface.PinCacheExpired
import org.openecard.sc.iface.ProtoMismatch
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.ReaderUnsupported
import org.openecard.sc.iface.RemovedCard
import org.openecard.sc.iface.ResetCard
import org.openecard.sc.iface.SecurityViolation
import org.openecard.sc.iface.ServerTooBusy
import org.openecard.sc.iface.ServiceStopped
import org.openecard.sc.iface.SharingViolation
import org.openecard.sc.iface.Shutdown
import org.openecard.sc.iface.SmartcardException
import org.openecard.sc.iface.SystemCancelled
import org.openecard.sc.iface.Timeout
import org.openecard.sc.iface.UnexpectedCardError
import org.openecard.sc.iface.UnknownCard
import org.openecard.sc.iface.UnknownError
import org.openecard.sc.iface.UnknownReader
import org.openecard.sc.iface.UnknownResMsg
import org.openecard.sc.iface.UnpoweredCard
import org.openecard.sc.iface.UnresponsiveCard
import org.openecard.sc.iface.UnsupportedCard
import org.openecard.sc.iface.UnsupportedFeature
import org.openecard.sc.iface.WaitedTooLong
import org.openecard.sc.iface.WriteTooMany
import org.openecard.sc.iface.WrongChv

internal inline fun <R> mapScioError(block: () -> R): R {
	try {
		return block.invoke()
	} catch (ex: PCSCError) {
		throw ex.toScException()
	}
}

internal fun PCSCError.toScException(): SmartcardException {
	val msg = message
	return when (this.error) {
		PCSCErrorCode.F_INTERNAL_ERROR -> InternalSystemError(msg, cause)
		PCSCErrorCode.E_CANCELLED -> Cancelled(msg, cause)
		PCSCErrorCode.E_INVALID_HANDLE -> InvalidHandle(msg, cause)
		PCSCErrorCode.E_INVALID_PARAMETER -> InvalidParameter(msg, cause)
		PCSCErrorCode.E_INVALID_TARGET -> InvalidTarget(msg, cause)
		PCSCErrorCode.E_NO_MEMORY -> NoMemory(msg, cause)
		PCSCErrorCode.F_WAITED_TOO_LONG -> WaitedTooLong(msg, cause)
		PCSCErrorCode.E_INSUFFICIENT_BUFFER -> InsufficientBuffer(msg, cause)
		PCSCErrorCode.E_UNKNOWN_READER -> UnknownReader(msg, cause)
		PCSCErrorCode.E_TIMEOUT -> Timeout(msg, cause)
		PCSCErrorCode.E_SHARING_VIOLATION -> SharingViolation(msg, cause)
		PCSCErrorCode.E_NO_SMARTCARD -> NoSmartcard(msg, cause)
		PCSCErrorCode.E_UNKNOWN_CARD -> UnknownCard(msg, cause)
		PCSCErrorCode.E_CANT_DISPOSE -> CantDispose(msg, cause)
		PCSCErrorCode.E_PROTO_MISMATCH -> ProtoMismatch(msg, cause)
		PCSCErrorCode.E_NOT_READY -> DeviceNotReady(msg, cause)
		PCSCErrorCode.E_INVALID_VALUE -> InvalidValue(msg, cause)
		PCSCErrorCode.E_SYSTEM_CANCELLED -> SystemCancelled(msg, cause)
		PCSCErrorCode.F_COMM_ERROR -> CommError(msg, cause)
		PCSCErrorCode.F_UNKNOWN_ERROR -> UnknownError(msg, cause)
		PCSCErrorCode.E_INVALID_ATR -> InvalidAtr(msg, cause)
		PCSCErrorCode.E_NOT_TRANSACTED -> NotTransacted(msg, cause)
		PCSCErrorCode.E_READER_UNAVAILABLE -> ReaderUnavailable(msg, cause)
		PCSCErrorCode.P_SHUTDOWN -> Shutdown(msg, cause)
		PCSCErrorCode.E_PCI_TOO_SMALL -> PciTooSmall(msg, cause)
		PCSCErrorCode.E_READER_UNSUPPORTED -> ReaderUnsupported(msg, cause)
		PCSCErrorCode.E_DUPLICATE_READER -> DuplicateReader(msg, cause)
		PCSCErrorCode.E_CARD_UNSUPPORTED -> CardUnsupported(msg, cause)
		PCSCErrorCode.E_NO_SERVICE -> NoService(msg, cause)
		PCSCErrorCode.E_SERVICE_STOPPED -> ServiceStopped(msg, cause)
		PCSCErrorCode.E_UNEXPECTED -> UnexpectedCardError(msg, cause)
		PCSCErrorCode.E_UNSUPPORTED_FEATURE -> IccInstallation(msg, cause)
		PCSCErrorCode.E_ICC_INSTALLATION -> IccCreateorder(msg, cause)
		PCSCErrorCode.E_ICC_CREATEORDER -> UnsupportedFeature(msg, cause)
		PCSCErrorCode.E_DIR_NOT_FOUND -> DirNotFound(msg, cause)
		PCSCErrorCode.E_FILE_NOT_FOUND -> FileNotFound(msg, cause)
		PCSCErrorCode.E_NO_DIR -> NoDir(msg, cause)
		PCSCErrorCode.E_NO_FILE -> NoFile(msg, cause)
		PCSCErrorCode.E_NO_ACCESS -> NoAccess(msg, cause)
		PCSCErrorCode.E_WRITE_TOO_MANY -> WriteTooMany(msg, cause)
		PCSCErrorCode.E_BAD_SEEK -> BadSeek(msg, cause)
		PCSCErrorCode.E_INVALID_CHV -> InvalidChv(msg, cause)
		PCSCErrorCode.E_UNKNOWN_RES_MNG -> UnknownResMsg(msg, cause)
		PCSCErrorCode.E_NO_SUCH_CERTIFICATE -> NoSuchCertificate(msg, cause)
		PCSCErrorCode.E_CERTIFICATE_UNAVAILABLE -> CertificateUnavailable(msg, cause)
		PCSCErrorCode.E_NO_READERS_AVAILABLE -> NoReadersAvailable(msg, cause)
		PCSCErrorCode.E_COMM_DATA_LOST -> CommDataLost(msg, cause)
		PCSCErrorCode.E_NO_KEY_CONTAINER -> NoKeyContainer(msg, cause)
		PCSCErrorCode.E_SERVER_TOO_BUSY -> ServerTooBusy(msg, cause)
		PCSCErrorCode.W_UNSUPPORTED_CARD -> UnsupportedCard(msg, cause)
		PCSCErrorCode.W_UNRESPONSIVE_CARD -> UnresponsiveCard(msg, cause)
		PCSCErrorCode.W_UNPOWERED_CARD -> UnpoweredCard(msg, cause)
		PCSCErrorCode.W_RESET_CARD -> ResetCard(msg, cause)
		PCSCErrorCode.W_REMOVED_CARD -> RemovedCard(msg, cause)
		PCSCErrorCode.W_SECURITY_VIOLATION -> SecurityViolation(msg, cause)
		PCSCErrorCode.W_WRONG_CHV -> WrongChv(msg, cause)
		PCSCErrorCode.W_CHV_BLOCKED -> ChvBlocked(msg, cause)
		PCSCErrorCode.W_EOF -> Eof(msg, cause)
		PCSCErrorCode.W_CANCELLED_BY_USER -> CancelledByUser(msg, cause)
		PCSCErrorCode.W_CARD_NOT_AUTHENTICATED -> CardNotAuthenticated(msg, cause)

		else -> {
			// handle some extra codes
			// TODO: when the extras are added, to the enum, move internal error to null branch
			when (code) {
				0x80100032 -> PinCacheExpired(msg, cause)
				0x80100033 -> NoPinCache(msg, cause)
				0x80100034 -> CardReadOnly(msg, cause)
				0x80100070 -> CacheItemNotFound(msg, cause)
				0x80100071 -> CacheItemStale(msg, cause)
				0x80100072 -> CacheItemTooBig(msg, cause)
				else -> InternalSystemError(msg, cause)
			}
		}
	}
}
