package org.openecard.common.ifd

abstract class RecoverableSecureMessagingException(
	msg: String,
	val errorResponse: ByteArray,
	cause: Throwable? = null,
) : Exception(msg, cause)

abstract class UnrecoverableSecureMessagingException(
	msg: String,
	cause: Throwable? = null,
) : RuntimeException(msg, cause)

class InvalidInputApduInSecureMessaging(
	msg: String,
	errorResponse: ByteArray,
	cause: Throwable? = null,
) : RecoverableSecureMessagingException(msg, errorResponse, cause)

class SecureMessagingParseException(
	msg: String,
	cause: Throwable? = null,
) : UnrecoverableSecureMessagingException(msg, cause)

class UnsupportedSecureMessagingFeature(
	msg: String,
	cause: Throwable? = null,
) : UnrecoverableSecureMessagingException(msg, cause)

class SecureMessagingCryptoException(
	msg: String,
	cause: Throwable? = null,
) : UnrecoverableSecureMessagingException(msg, cause)

class SecureMessagingRejectedByIcc(
	msg: String,
	cause: Throwable? = null,
) : UnrecoverableSecureMessagingException(msg, cause)

class MissingSecureMessagingChannel(
	msg: String,
	cause: Throwable? = null,
) : UnrecoverableSecureMessagingException(msg, cause)
