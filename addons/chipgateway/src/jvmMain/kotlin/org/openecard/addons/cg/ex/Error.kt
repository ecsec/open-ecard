package org.openecard.addons.cg.ex

import org.openecard.addon.bind.AuxDataKeys
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode

/**
 * The superclass of all errors which are visible to the activation action of the plug-in.
 * It has the capability to produce a BindingResult representing the error in an appropriate way.
 *
 * @author Tobias Wich
 */
abstract class ActivationError(
	val bindingResult: BindingResult,
	message: String,
	cause: Throwable?,
) : Exception(message, cause)

/**
 * Exception indicating an error received from the server.
 *
 * @author Tobias Wich
 */
class AuthServerException(
	errorUrl: String?,
	msg: String,
	ex: Throwable?,
) : RedirectionBaseError(errorUrl, msg, ex)

/**
 * Exception indicating that a redirect of the caller will be performed.
 *
 * @author Tobias Wich
 */
abstract class RedirectionBaseError(
	errorUrl: String?,
	msg: String,
	ex: Throwable?,
) : ActivationError(
		BindingResult(BindingResultCode.REDIRECT, msg).apply {
			addAuxResultData(AuxDataKeys.REDIRECT_LOCATION, errorUrl)
		},
		msg,
		ex,
	)

class ChipGatewayDataError(
	errorUrl: String?,
	msg: String,
	ex: Throwable? = null,
) : RedirectionBaseError(errorUrl, msg, ex)

class ChipGatewayUnknownError(
	errorUrl: String?,
	msg: String,
	ex: Throwable? = null,
) : RedirectionBaseError(errorUrl, msg, ex)

class ConnectionError(
	errorUrl: String?,
	msg: String,
	ex: Throwable? = null,
) : RedirectionBaseError(errorUrl, msg, ex)

/**
 * Specialization of an ActivationError which does not permit the user to continue after returning to the Browser.
 *
 * @author Tobias Wich
 */
abstract class FatalActivationError(
	val result: BindingResult,
	message: String,
	cause: Throwable? = null,
) : ActivationError(result, message, cause)

/**
 * Error indicating a missing or invalid redirect URL for errors.
 *
 * @author Tobias Wich
 */
class InvalidRedirectUrlException(
	msg: String,
	ex: Throwable? = null,
) : FatalActivationError(
		BindingResult(BindingResultCode.WRONG_PARAMETER),
		msg,
		ex,
	)

class InvalidSubjectException(
	msg: String?,
	cause: Throwable? = null,
) : Exception(msg, cause)

/**
 * Exception indicating a missing or errornous element in the TCToken.
 *
 * @author Tobias Wich
 */
class InvalidTCTokenElement(
	msg: String,
	ex: Throwable? = null,
) : FatalActivationError(
		BindingResult(
			BindingResultCode.WRONG_PARAMETER,
			msg,
		),
		msg,
		ex,
	)

class ParameterInvalid(
	msg: String?,
	cause: Throwable? = null,
) : Exception(msg, cause)

class PinBlocked(
	msg: String?,
	cause: Throwable? = null,
) : Exception(msg, cause)

class RemotePinException(
	msg: String?,
	cause: Throwable? = null,
) : Exception(msg, cause)

class SlotHandleInvalid(
	msg: String?,
	cause: Throwable? = null,
) : Exception(msg, cause)

class VersionTooOld(
	errorUrl: String?,
	msg: String,
	ex: Throwable? = null,
) : RedirectionBaseError(errorUrl, msg, ex)
