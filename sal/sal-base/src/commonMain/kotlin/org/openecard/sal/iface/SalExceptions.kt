package org.openecard.sal.iface

sealed class SalException(
	msg: String,
	cause: Throwable?,
) : Exception(msg, cause)

class InternalSystemError(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "An internal consistency check failed.", cause)

class NotInitialized(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "The underlying stack is not initialized.", cause)

class NoService(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "The underlying stack is not available.", cause)

class DeviceUnavailable(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "The requested device is not known or unavailable.", cause)

class RemovedDevice(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "The device has been removed, so further communication is not possible.", cause)

class DeviceUnsupported(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "The requested device is not supported by the SAL.", cause)

class Timeout(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "The call timed out.", cause)

class Cancelled(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "The call has been cancelled.", cause)

class SharingViolation(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "The device cannot be accessed because of other connections outstanding.", cause)

class UnsupportedFeature(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "The device does not support the requested feature.", cause)

class MissingAuthentication(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "Authentication missing in order to access the requested entity.", cause)

class SecureMessagingException(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "Error in secure messaging processing.", cause)

class AclUnfulfillable(
	msg: String? = null,
	cause: Throwable? = null,
) : SalException(msg ?: "Acl can not be fulfilled.", cause)
