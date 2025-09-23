package org.openecard.addons.tr03124

sealed interface BindingResponse {
	val status: Int

	class RedirectResponse(
		override val status: Int,
		val redirectUrl: String,
	) : BindingResponse

	class ReferencedContentResponse(
		override val status: Int,
		val payload: ContentCode,
	) : BindingResponse

	class ContentResponse(
		override val status: Int,
		val contentType: String,
		val payload: ByteArray,
	) : BindingResponse

	class NoContent(
		override val status: Int = 204,
	) : BindingResponse

	enum class ContentCode {
		NO_SUITABLE_ACTIVATION_PARAMETERS,
		TC_TOKEN_RETRIEVAL_ERROR,
		COMMUNICATION_ERROR,
		OTHER_PROCESS_RUNNING,
		INTERNAL_ERROR,
	}
}
