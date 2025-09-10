package org.openecard.addons.tr03124

import io.ktor.http.HttpStatusCode
import org.openecard.addons.tr03124.transport.EserviceClient

sealed class BindingException(
	msg: String,
	cause: Throwable?,
) : Exception(msg, cause) {
	/**
	 * Get the BindingResponse from this exception, so the context can be returned to the calling application.
	 * This function might resolve the final redirect and attaches error codes to it, so there is nothing more to do
	 * with the response.
	 * Or it might return a content response directly to the caller.
	 */
	abstract suspend fun toResponse(): BindingResponse
}

sealed class AbstractBindingException(
	protected val eserviceClient: EserviceClient,
	msg: String,
	cause: Throwable?,
	val minorCode: String,
	val errorMsg: String? = null,
) : BindingException(msg, cause) {
	override suspend fun toResponse(): BindingResponse = eserviceClient.redirectToEservice(minorCode, errorMsg)
}

class NoTcToken(
	msg: String,
	cause: Throwable? = null,
) : BindingException(msg, cause) {
	override suspend fun toResponse(): BindingResponse =
		BindingResponse.ContentResponse(
			HttpStatusCode.NotFound.value,
			BindingResponse.ContentCode.TC_TOKEN_RETRIEVAL_ERROR,
		)
}

sealed class TrustedChannelEstablishmentError(
	eserviceClient: EserviceClient,
	msg: String? = null,
	cause: Throwable? = null,
	errorMsg: String? = null,
) : AbstractBindingException(
		eserviceClient,
		msg ?: "The eID-Client failed to set up a trusted channel to the eID-Server",
		cause,
		"trustedChannelEstablishmentFailed",
		errorMsg = errorMsg,
	)

class TlsError(
	eserviceClient: EserviceClient,
	tlsErrorCode: String,
	msg: String? = null,
	cause: Throwable? = null,
) : TrustedChannelEstablishmentError(
		eserviceClient,
		msg ?: "Failed to establish TLS connection",
		cause,
		errorMsg = tlsErrorCode,
	)

class UnknownTrustedChannelError(
	eserviceClient: EserviceClient,
	msg: String? = null,
	cause: Throwable? = null,
) : TrustedChannelEstablishmentError(
		eserviceClient,
		msg ?: "The eID-Client failed to set up a trusted channel to the eID-Server",
		cause,
	)

class UserCanceled(
	eserviceClient: EserviceClient,
	msg: String? = null,
	cause: Throwable? = null,
) : AbstractBindingException(eserviceClient, msg ?: "The user aborted the authentication", cause, "cancellationByUser")

sealed class ServerError(
	eserviceClient: EserviceClient,
	msg: String,
	cause: Throwable? = null,
) : AbstractBindingException(eserviceClient, msg, cause, "serverError")

class InvalidServerData(
	eserviceClient: EserviceClient,
	msg: String? = null,
	cause: Throwable? = null,
) : ServerError(eserviceClient, msg ?: "The eID-Server sent invalid data", cause)

class UnkownServerError(
	eserviceClient: EserviceClient,
	msg: String? = null,
	cause: Throwable? = null,
) : ServerError(eserviceClient, msg ?: "The eID-Server encountered an error", cause)

class ClientError(
	eserviceClient: EserviceClient,
	msg: String? = null,
	cause: Throwable? = null,
) : AbstractBindingException(
		eserviceClient,
		msg ?: "Any error not covered by the other error codes occurred",
		cause,
		"clientError",
	)
