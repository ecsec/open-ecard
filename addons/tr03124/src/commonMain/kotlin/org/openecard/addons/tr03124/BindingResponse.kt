package org.openecard.addons.tr03124

interface BindingResponse {
	val status: Int

	class RedirectResponse(
		override val status: Int,
		val redirectUrl: String,
	) : BindingResponse

	class ContentResponse(
		override val status: Int,
		val payload: Payload,
	) : BindingResponse

	class Payload(
		val mimeType: String,
		val data: String,
	)
}
