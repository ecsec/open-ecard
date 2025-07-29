package org.openecard.sal.sc

import org.openecard.sal.iface.DeviceUnavailable
import org.openecard.sal.iface.DeviceUnsupported
import org.openecard.sal.iface.MissingAuthentication
import org.openecard.sal.iface.SalException
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

fun <T> mapSmartcardError(block: () -> T): T {
	try {
		return block()
	} catch (ex: SmartcardException) {
		throw ex.toSalException()
	}
}

suspend fun <T> mapSmartcardErrorSuspending(block: suspend () -> T): T {
	try {
		return block()
	} catch (ex: SmartcardException) {
		throw ex.toSalException()
	}
}

internal fun SmartcardException.toSalException(): SalException =
	when (this) {
		is CancelledByUser,
		is Cancelled,
		->
			org.openecard.sal.iface
				.Cancelled(cause = this)
		is CardNotAuthenticated -> MissingAuthentication(cause = this)
		is NoService,
		is ServiceStopped,
		->
			org.openecard.sal.iface
				.NoService(cause = this)
		is NotTransacted,
		is SharingViolation,
		->
			org.openecard.sal.iface
				.SharingViolation(cause = this)
		is WaitedTooLong,
		is Timeout,
		->
			org.openecard.sal.iface
				.Timeout(cause = this)
		is DeviceNotReady,
		is RemovedCard,
		is ResetCard,
		is CardReadOnly,
		is ReaderUnavailable,
		is NoReadersAvailable,
		is NoSmartcard,
		is UnknownReader,
		is UnpoweredCard,
		is UnresponsiveCard,
		-> DeviceUnavailable(cause = this)
		is ReaderUnsupported,
		is CardUnsupported,
		is UnknownCard,
		is UnsupportedCard,
		-> DeviceUnsupported(cause = this)
		is UnsupportedFeature ->
			org.openecard.sal.iface
				.UnsupportedFeature(cause = this)
		is BadSeek,
		is ChvBlocked,
		is CommDataLost,
		is CommError,
		is PciTooSmall,
		is PinCacheExpired,
		is ProtoMismatch,
		is SecurityViolation,
		is DirNotFound,
		is DuplicateReader,
		is Eof,
		is NoSuchCertificate,
		is CertificateUnavailable,
		is FileNotFound,
		is IccCreateorder,
		is IccInstallation,
		is InsufficientBuffer,
		is InternalSystemError,
		is InvalidAtr,
		is InvalidChv,
		is InvalidHandle,
		is InvalidParameter,
		is InvalidTarget,
		is InvalidValue,
		is NoAccess,
		is NoDir,
		is NoFile,
		is NoKeyContainer,
		is NoMemory,
		is NoPinCache,
		is ServerTooBusy,
		is Shutdown,
		is SystemCancelled,
		is CacheItemNotFound,
		is CacheItemStale,
		is CacheItemTooBig,
		is CantDispose,
		is UnexpectedCardError,
		is UnknownError,
		is UnknownResMsg,
		is WriteTooMany,
		is WrongChv,
		->
			org.openecard.sal.iface
				.InternalSystemError(cause = this)
	}
