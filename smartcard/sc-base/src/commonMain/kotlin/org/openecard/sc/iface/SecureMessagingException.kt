package org.openecard.sc.iface

sealed class SecureMessagingException(
	msg: String? = null,
	cause: Throwable? = null,
) : Exception(msg ?: "There was an error while executing secure messaging", cause)

class UnknownSecureMessagingError(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Unknown secure messaging error occurred", cause)

class SecureMessagingUnsupported(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Secure messaging is unsupported", cause)

class InvalidApduStatus(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Secure messaging response APDU does not contain a valid status", cause)

class NoSwData(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "No SW data object found in secure messaging DOs", cause)

class InvalidSwData(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Invalid SW data object found in secure messaging DOs", cause)

class SequenceCounterOverflow(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Sequence counter overflow detected", cause)

class CryptographicChecksumMissing(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Cryptographic checksum is not present in DOs", cause)

class CryptographicChecksumWrong(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Cryptographic checksum does not match calculated value", cause)

class MissingSmDo(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Secure messaging DO is missing", cause)

class InvalidSmDo(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Invalid secure messaging DO encountered", cause)

class UnsupportedSmDo(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Unsupported secure messaging DO encountered", cause)

class UnsupportedPadding(
	msg: String? = null,
	cause: Throwable? = null,
) : SecureMessagingException(msg ?: "Unsupported padding in encrypted DO", cause)
