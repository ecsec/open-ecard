package org.openecard.addons.tr03124

interface Tr03124Binding {
	suspend fun activate(tcTokenUrl: String): Response

	suspend fun status(): Response

	suspend fun showUi(module: Parameter.ShowUi.ShowUiModules)

	class Response(
		val status: Int,
		val payload: Payload? = null,
		val redirectUrl: String? = null,
	) {
		companion object {
			// fun makeResponse(redirectUrl: String)
		}
	}

	class Payload(
		val mimeType: String,
		val data: String,
	)

	object Parameter {
		val serverPath = "/eID-Client"

		object Activate {
			val tcTokenUrl = "tcTokenURL"
		}

		object Status {
			val status = "Status"
		}

		object ShowUi {
			val showUi = "ShowUI"

			enum class ShowUiModules(
				val code: String,
			) {
				PIN_MANAGEMENT("PINManagement"),
				SETTINGS("Settings"),
				UNKNOWN("MainScreen"),
				;

				companion object {
					fun String.toUiModule(): ShowUiModules =
						entries.find { it.code == this }
							?: UNKNOWN
				}
			}
		}
	}
}
