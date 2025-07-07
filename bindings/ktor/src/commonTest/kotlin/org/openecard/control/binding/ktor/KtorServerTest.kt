package org.openecard.control.binding.ktor

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class SomeError(
	val type: String,
	val message: String,
)

fun Application.configureSut(
	port: Int = 24727,
	host: String = "0.0.0.0",
	corsOrigins: Set<String> = setOf("service.skidentity.de"),
	configuration: Routing.() -> Unit = {},
) {
	configureServer(
		port = port,
		host = host,
		corsOrigins = corsOrigins,
	) {
		configuration()
	}
}

class KtorServerTest {
	@Test
	fun testMinimumError() =
		testApplication {
			application {
				configureSut {
				}
			}
			routing {
				get("/example-error") {
					call.respond(status = HttpStatusCode.BadRequest, "Some sort of error occurred")
				}
			}

			val response = client.get("/example-error")
			assertEquals(HttpStatusCode.BadRequest, response.status)
			assertEquals(ContentType.Text.Html.withCharset(Charsets.UTF_8), response.contentType())
		}

	@Test
	fun testPlainError() =
		testApplication {
			application {
				configureSut {
				}
			}
			routing {
				get("/example-error") {
					call.respondText(ContentType.Text.Plain, status = HttpStatusCode.BadRequest) {
						"Some sort of error occurred"
					}
				}
			}

			val response = client.get("/example-error")
			assertEquals(HttpStatusCode.BadRequest, response.status)
			assertEquals(ContentType.Text.Plain.withCharset(Charsets.UTF_8), response.contentType())
		}

	@Test
	fun testDataClassError() =
		testApplication {
			application {
				configureSut {
				}
			}
			routing {
				get("/example-error") {
					call.respond(SomeError(type = "Magic", message = "Delicious"))
					call.response.status(HttpStatusCode.BadRequest)
				}
			}

			val response = client.get("/example-error")
			assertEquals(HttpStatusCode.BadRequest, response.status)
			assertEquals("Customer stored correctly", response.bodyAsText())
			assertEquals(ContentType.Text.Plain.withCharset(Charsets.UTF_8), response.contentType())
		}
}
