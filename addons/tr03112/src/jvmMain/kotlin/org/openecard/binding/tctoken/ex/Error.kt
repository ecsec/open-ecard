package org.openecard.binding.tctoken.ex

import org.openecard.addon.bind.AuxDataKeys
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode

/**
 * Exception indicating an error received from the server.
 *
 * @author Tobias Wich
 */
class AuthServerException(
	errorUrl: String?,
	msg: String,
	ex: Throwable? = null,
) : RedirectionBaseError(errorUrl, msg, ex)

/**
 * The superclass of all errors which are visible to the activation action of the plug-in.
 * It has the capability to produce a BindingResult representing the error in an appropriate way.
 *
 * @author Tobias Wich
 */
abstract class ActivationError(
	val bindingResult: BindingResult,
	message: String?,
	cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Specialization of an ActivationError which does not permit the user to continue after returning to the Browser.
 *
 * @author Tobias Wich
 */
abstract class FatalActivationError(
	result: BindingResult,
	message: String,
	cause: Throwable?,
) : ActivationError(result, message, cause)

/**
 * Exception indicating an invalid activation endpoint address.
 *
 * @author Hans-Martin Haase
 */
class InvalidAddressException(
	msg: String,
	ex: Throwable? = null,
) : FatalActivationError(
		BindingResult(BindingResultCode.WRONG_PARAMETER),
		msg,
		ex,
	)

/**
 * Error indicating a missing redirect URL for errors.
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

/**
 * Exception indicating a missing or errornous element in the TCToken.
 *
 * @author Tobias Wich
 */
class InvalidTCTokenElement(
	errorUrl: String?,
	msg: String? = null,
	ex: Throwable? = null,
) : RedirectionBaseError(errorUrl, msg, ex)

/**
 * Exception indicating an invalid TCToken.
 * Possible conditions are e.g. the TCToken is not present or could not be parsed.
 *
 * @author Tobias Wich
 */
open class InvalidTCTokenException(
	msg: String,
	ex: Throwable? = null,
) : FatalActivationError(
		BindingResult(BindingResultCode.RESOURCE_UNAVAILABLE),
		msg,
		ex,
	)

/**
 * Exception indicating an invalid URL inside a TCToken.
 *
 * @author Tobias Wich
 */
class InvalidTCTokenUrlException(
	msg: String,
	ex: Throwable? = null,
) : InvalidTCTokenException(
		msg,
		ex,
	)

/**
 * Exception indication that the activation request is malformed.
 *
 * @author Tobias Wich
 */
class MissingActivationParameterException(
	msg: String,
	ex: Throwable? = null,
) : FatalActivationError(
		BindingResult(BindingResultCode.MISSING_PARAMETER),
		msg,
		ex,
	)

/**
 *
 * @author Hans-Martin Haase
 */
class NonGuiException(
	result: BindingResult,
	message: String,
	cause: Throwable? = null,
) : ActivationError(result, message, cause)

/**
 * Exception indicating that a redirect of the caller will be performed.
 *
 * @author Tobias Wich
 */
abstract class RedirectionBaseError(
	errorUrl: String?,
	msg: String? = null,
	ex: Throwable? = null,
) : ActivationError(
		BindingResult(BindingResultCode.REDIRECT).apply {
			addAuxResultData(AuxDataKeys.REDIRECT_LOCATION, errorUrl)
		},
		msg,
		ex,
	)

/**
 * Exception indicating a security violation.
 * This can be a violation of the same origin policy or something similar.
 *
 * @author Tobias Wich
 */
class SecurityViolationException(
	errorUrl: String?,
	msg: String,
	ex: Throwable? = null,
) : RedirectionBaseError(errorUrl, msg, ex)

/**
 * Exception indicating a failure to retrieve the TCToken from the given URL.
 *
 * @author Tobias Wich
 */
class TCTokenRetrievalException(
	msg: String,
	ex: Throwable?,
) : InvalidTCTokenException(
		msg,
		ex,
	)

/**
 *
 * @author Hans-Martin Haase
 */
class UserCancellationException(
	errorUrl: String?,
	cause: Throwable,
) : RedirectionBaseError(errorUrl, ex = cause)
