package org.openecard.sc.pcsc

import jnasmartcardio.Smartcardio.EstablishContextException
import jnasmartcardio.Smartcardio.JnaCardException
import jnasmartcardio.Smartcardio.JnaCardNotPresentException
import jnasmartcardio.Smartcardio.JnaPCSCException
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
import org.openecard.sc.iface.LogicalChannelException
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
import javax.smartcardio.CardException
import javax.smartcardio.CardNotPresentException

fun <R> mapScioError(block: () -> R): R {
	try {
		return block.invoke()
	} catch (ex: EstablishContextException) {
		throw ex.cause!!.toScException()
	} catch (ex: JnaCardNotPresentException) {
		throw ex.toScException()
	} catch (ex: CardNotPresentException) {
		throw ex.toScException()
	} catch (ex: JnaPCSCException) {
		throw ex.toScException()
	} catch (ex: JnaCardException) {
		throw LogicalChannelException(ex.message, ex)
	} catch (ex: CardException) {
		throw InternalSystemError(ex.message, ex.cause)
	}
}

internal fun JnaPCSCException.toScException(): SmartcardException = mapToScException(code, message, this)

internal fun JnaCardNotPresentException.toScException(): SmartcardException = mapToScException(code, message, this)

internal fun CardNotPresentException.toScException(): SmartcardException = NoSmartcard(message, this)

fun mapToScException(
	code: Long,
	msg: String?,
	cause: Throwable,
): SmartcardException =
	when (code) {
		0x80100001 -> InternalSystemError(msg, cause)
		0x80100002 -> Cancelled(msg, cause)
		0x80100003 -> InvalidHandle(msg, cause)
		0x80100004 -> InvalidParameter(msg, cause)
		0x80100005 -> InvalidTarget(msg, cause)
		0x80100006 -> NoMemory(msg, cause)
		0x80100007 -> WaitedTooLong(msg, cause)
		0x80100008 -> InsufficientBuffer(msg, cause)
		0x80100009 -> UnknownReader(msg, cause)
		0x8010000A -> Timeout(msg, cause)
		0x8010000B -> SharingViolation(msg, cause)
		0x8010000C -> NoSmartcard(msg, cause)
		0x8010000D -> UnknownCard(msg, cause)
		0x8010000E -> CantDispose(msg, cause)
		0x8010000F -> ProtoMismatch(msg, cause)
		0x80100010 -> DeviceNotReady(msg, cause)
		0x80100011 -> InvalidValue(msg, cause)
		0x80100012 -> SystemCancelled(msg, cause)
		0x80100013 -> CommError(msg, cause)
		0x80100014 -> UnknownError(msg, cause)
		0x80100015 -> InvalidAtr(msg, cause)
		0x80100016 -> NotTransacted(msg, cause)
		0x80100017 -> ReaderUnavailable(msg, cause)
		0x80100018 -> Shutdown(msg, cause)
		0x80100019 -> PciTooSmall(msg, cause)
		0x8010001A -> ReaderUnsupported(msg, cause)
		0x8010001B -> DuplicateReader(msg, cause)
		0x8010001C -> CardUnsupported(msg, cause)
		0x8010001D -> NoService(msg, cause)
		0x8010001E -> ServiceStopped(msg, cause)
		0x8010001F -> UnexpectedCardError(msg, cause)
		0x80100020 -> IccInstallation(msg, cause)
		0x80100021 -> IccCreateorder(msg, cause)
		0x80100022 -> UnsupportedFeature(msg, cause)
		0x80100023 -> DirNotFound(msg, cause)
		0x80100024 -> FileNotFound(msg, cause)
		0x80100025 -> NoDir(msg, cause)
		0x80100026 -> NoFile(msg, cause)
		0x80100027 -> NoAccess(msg, cause)
		0x80100028 -> WriteTooMany(msg, cause)
		0x80100029 -> BadSeek(msg, cause)
		0x8010002A -> InvalidChv(msg, cause)
		0x8010002B -> UnknownResMsg(msg, cause)
		0x8010002C -> NoSuchCertificate(msg, cause)
		0x8010002D -> CertificateUnavailable(msg, cause)
		0x8010002E -> NoReadersAvailable(msg, cause)
		0x8010002F -> CommDataLost(msg, cause)
		0x80100030 -> NoKeyContainer(msg, cause)
		0x80100031 -> ServerTooBusy(msg, cause)
		0x80100032 -> PinCacheExpired(msg, cause)
		0x80100033 -> NoPinCache(msg, cause)
		0x80100034 -> CardReadOnly(msg, cause)
		0x80100065 -> UnsupportedCard(msg, cause)
		0x80100066 -> UnresponsiveCard(msg, cause)
		0x80100067 -> UnpoweredCard(msg, cause)
		0x80100068 -> ResetCard(msg, cause)
		0x80100069 -> RemovedCard(msg, cause)
		0x8010006A -> SecurityViolation(msg, cause)
		0x8010006B -> WrongChv(msg, cause)
		0x8010006C -> ChvBlocked(msg, cause)
		0x8010006D -> Eof(msg, cause)
		0x8010006E -> CancelledByUser(msg, cause)
		0x8010006F -> CardNotAuthenticated(msg, cause)
		0x80100070 -> CacheItemNotFound(msg, cause)
		0x80100071 -> CacheItemStale(msg, cause)
		0x80100072 -> CacheItemTooBig(msg, cause)

		else -> InternalSystemError(msg, cause)
	}
