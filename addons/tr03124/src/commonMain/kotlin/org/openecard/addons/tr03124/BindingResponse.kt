package org.openecard.addons.tr03124

interface BindingResponse {
	val status: Int

	class RedirectResponse(
		override val status: Int,
		val redirectUrl: String,
	) : BindingResponse

	class ContentResponse(
		override val status: Int,
		val payload: ContentCode,
	) : BindingResponse

	enum class ContentCode {
		TC_TOKEN_RETRIEVAL_ERROR,
		COMMUNICATION_ERROR,
	}
}
