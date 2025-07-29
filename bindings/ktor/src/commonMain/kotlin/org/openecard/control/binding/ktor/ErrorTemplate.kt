package org.openecard.control.binding.ktor

import io.ktor.http.headers
import io.ktor.server.html.Placeholder
import io.ktor.server.html.Template
import io.ktor.server.html.insert
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.TITLE
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.title
import kotlinx.serialization.json.JsonNull.content

class ErrorTemplate : Template<HTML> {
	val errorTitle = Placeholder<TITLE>()
	val headline = Placeholder<FlowContent>()
	val message = Placeholder<FlowContent>()

	override fun HTML.apply() {
		headers {
			head {
				title {
					insert(errorTitle)
				}
				meta {
					httpEquiv = "Content-Type"
					content = "text/html; charset=UTF-8"
				}
				link {
					rel = "SHORTCUT ICON"
					href = "favicon.ico"
				}
				link {
					rel = "stylesheet"
					href = "./css/style.css"
					type = "text/css"
				}
			}
		}
		body {
			div("box") {
				h1 {
					insert(headline)
				}
				p {
					insert(message)
				}
			}
		}
	}
}
