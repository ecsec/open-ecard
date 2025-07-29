package org.openecard.sc.iface

sealed class SmartcardException(
	msg: String,
	cause: Throwable?,
) : Exception(msg, cause)

class SmartCardStackMissing(
	msg: String,
	cause: Throwable?,
) : Exception(msg, cause)

class LogicalChannelException(
	msg: String?,
	cause: Throwable?,
) : Exception(msg ?: "Error with logical channel.", cause)

class InternalSystemError(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "An internal consistency check failed.", cause)

class Cancelled(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The action was cancelled by a cancel request.", cause)

class InvalidHandle(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The supplied handle was invalid.", cause)

class InvalidParameter(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "One or more of the supplied parameters could not be properly interpreted.", cause)

class InvalidTarget(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "Registry startup information is missing or invalid.", cause)

class NoMemory(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "Not enough memory available to complete this command.", cause)

class WaitedTooLong(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "An internal consistency timer has expired.", cause)

class InsufficientBuffer(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The data buffer to receive returned data is too small for the returned data.", cause)

class UnknownReader(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The specified reader name is not recognized.", cause)

class Timeout(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The user-specified timeout value has expired.", cause)

class SharingViolation(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The smart card cannot be accessed because of other connections outstanding.", cause)

class NoSmartcard(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(
		msg ?: "The operation requires a Smart Card, but no Smart Card is currently in the device.",
		cause,
	)

class UnknownCard(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The specified smart card name is not recognized.", cause)

class CantDispose(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The system could not dispose of the media in the requested manner.", cause)

class ProtoMismatch(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(
		msg ?: "The requested protocols are incompatible with the protocol currently in use with the smart card.",
		cause,
	)

class DeviceNotReady(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The reader or smart card is not ready to accept commands.", cause)

class InvalidValue(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "One or more of the supplied parameters values could not be properly interpreted.", cause)

class SystemCancelled(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The action was cancelled by the system, presumably to log off or shut down.", cause)

class CommError(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "An internal communications error has been detected.", cause)

class UnknownError(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "An internal error has been detected, but the source is unknown.", cause)

class InvalidAtr(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "An ATR obtained from the registry is not a valid ATR string.", cause)

class NotTransacted(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "An attempt was made to end a non-existent transaction.", cause)

class ReaderUnavailable(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The specified reader is not currently available for use.", cause)

class Shutdown(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The operation has been aborted to allow the server application to exit.", cause)

class PciTooSmall(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The PCI Receive buffer was too small.", cause)

class ReaderUnsupported(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The reader driver does not meet minimal requirements for support.", cause)

class DuplicateReader(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The reader driver did not produce a unique reader name.", cause)

class CardUnsupported(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The smart card does not meet minimal requirements for support.", cause)

class NoService(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The Smart card resource manager is not running.", cause)

class ServiceStopped(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The Smart card resource manager has shut down.", cause)

class UnexpectedCardError(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "An unexpected card error has occurred.", cause)

class UnsupportedFeature(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "This smart card does not support the requested feature.", cause)

class IccInstallation(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "No primary provider can be found for the smart card.", cause)

class IccCreateorder(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The requested order of object creation is not supported.", cause)

class DirNotFound(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The identified directory does not exist in the smart card.", cause)

class FileNotFound(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The identified file does not exist in the smart card.", cause)

class NoDir(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The supplied path does not represent a smart card directory.", cause)

class NoFile(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The supplied path does not represent a smart card file.", cause)

class NoAccess(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "Access is denied to this file.", cause)

class WriteTooMany(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The smart card does not have enough memory to store the information.", cause)

class BadSeek(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "There was an error trying to set the smart card file object pointer.", cause)

class InvalidChv(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The supplied PIN is incorrect.", cause)

class UnknownResMsg(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "An unrecognized error code was returned from a layered component.", cause)

class NoSuchCertificate(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The requested certificate does not exist.", cause)

class CertificateUnavailable(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The requested certificate could not be obtained.", cause)

class NoReadersAvailable(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "Cannot find a smart card reader.", cause)

class CommDataLost(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "A communications error with the smart card has been detected.", cause)

class NoKeyContainer(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The requested key container does not exist on the smart card.", cause)

class ServerTooBusy(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The Smart Card Resource Manager is too busy to complete this operation.", cause)

class PinCacheExpired(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The smart card PIN cache has expired.", cause)

class NoPinCache(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The smart card PIN cannot be cached.", cause)

class CardReadOnly(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The smart card is read-only and cannot be written to.", cause)

class UnsupportedCard(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(
		msg ?: "The reader cannot communicate with the card, due to ATR string configuration conflicts.",
		cause,
	)

class UnresponsiveCard(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The smart card is not responding to a reset.", cause)

class UnpoweredCard(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(
		msg ?: "Power has been removed from the smart card, so that further communication is not possible.",
		cause,
	)

class ResetCard(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The smart card has been reset, so any shared state information is invalid.", cause)

class RemovedCard(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The smart card has been removed, so further communication is not possible.", cause)

class SecurityViolation(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "Access was denied because of a security violation.", cause)

class WrongChv(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The card cannot be accessed because the wrong PIN was presented.", cause)

class ChvBlocked(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(
		msg ?: "The card cannot be accessed because the maximum number of PIN entry attempts has been reached.",
		cause,
	)

class Eof(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The end of the smart card file has been reached.", cause)

class CancelledByUser(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The user pressed 'Cancel' on a Smart Card Selection Dialog.", cause)

class CardNotAuthenticated(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "No PIN was presented to the smart card.", cause)

class CacheItemNotFound(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The requested item could not be found in the cache.", cause)

class CacheItemStale(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The requested cache item is too old and was deleted from the cache.", cause)

class CacheItemTooBig(
	msg: String? = null,
	cause: Throwable? = null,
) : SmartcardException(msg ?: "The new cache item exceeds the maximum per-item size defined for the cache.", cause)
