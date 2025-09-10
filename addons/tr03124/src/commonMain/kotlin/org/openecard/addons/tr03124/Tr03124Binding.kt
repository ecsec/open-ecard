package org.openecard.addons.tr03124

interface Tr03124Binding {
	suspend fun activate(tcTokenUrl: String): BindingResponse

	suspend fun status(): BindingResponse

	suspend fun showUi(module: Parameter.ShowUi.ShowUiModules)

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
