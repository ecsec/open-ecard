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

fun assertStartsWith(
	expected: String,
	actual: String,
	message: String? = null,
) {
	assertEquals(expected, actual.take(expected.length), message)
}

@Serializable
data class SomeError(
	val type: String,
	val message: String,
)

fun Application.configureSut(
	port: Int = 24727,
	host: String = "localhost",
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
			assertStartsWith("<!DOCTYPE html>", response.bodyAsText())
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
			assertEquals(ContentType.Text.Html.withCharset(Charsets.UTF_8), response.contentType())
			assertStartsWith("<!DOCTYPE html>", response.bodyAsText())
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
					call.respond(HttpStatusCode.BadRequest, SomeError(type = "Magic", message = "Delicious"))
				}
			}

			val response = client.get("/example-error")
			assertEquals(HttpStatusCode.BadRequest, response.status)
			assertStartsWith("<!DOCTYPE html>", response.bodyAsText())
			assertEquals(ContentType.Text.Html.withCharset(Charsets.UTF_8), response.contentType())
		}
}
