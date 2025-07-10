package org.openecard.control.binding.ktor

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import org.openecard.i18n.I18N

private val logger = KotlinLogging.logger { }

fun extractContent(content: OutgoingContent): String? =
	when (content) {
		is OutgoingContent.ContentWrapper -> {
			extractContent(content.delegate())
		}
		is OutgoingContent.ByteArrayContent -> {
			content.bytes().toString()
		}
		else -> {
			logger.warn { "Unhandled outgoing of outgoing type: ${content.javaClass}" }
			null
		}
	}

private fun generateErrorCodes(): List<HttpStatusCode> {
	val result = mutableListOf<Int>()
	for (i in 400..417) {
		result.add(i)
	}

	// additional codes used by the HttpAppPluginActionHandler
	result.add(423) // Locked
	result.add(429) // Too many requests

	for (i in 500..505) {
		result.add(i)
	}
	return result.map { HttpStatusCode.fromValue(it) }
}

class ServerHeaderPluginConfig {
	lateinit var header: String
}

val ServerHeader =
	createApplicationPlugin<ServerHeaderPluginConfig>("ServerHeader", { ServerHeaderPluginConfig() }) {

		val headerValue = pluginConfig.header

		onCall { call ->
			call.response.headers.append("Server", headerValue)
		}
	}

val CacheControlHeader =
	createApplicationPlugin("CacheControlHeader") {
		onCall { call ->
			call.response.headers.append("Cache-Control", "no-store")
		}
	}

val SecurityHeader =
	createApplicationPlugin("SecurityHeader") {
		onCall { call ->
			val headers = call.response.headers
			headers.append("X-Custom-Header", "Hello, world!")
			headers.append("X-XSS-Protection", "1")
			headers.append(
				"Content-Security-Policy",
				"default-src 'none'; script-src 'none'; style-src 'self'; img-src 'self'",
			)
			headers.append("X-Content-Type-Options", "nosniff")
			headers.append("X-Frame-Options", "SAMEORIGIN")
		}
	}

fun Application.configureServer(
	port: Int,
	host: String,
	corsOrigins: Set<String>,
	serverAgent: String?,
	configuration: Routing.() -> Unit,
) {
	if (serverAgent != null) {
		install(ServerHeader) {
			header = serverAgent
		}
	}
	install(SecurityHeader)
	install(CacheControlHeader)
	install(CORS) {
		allowHost("$host:$port")
		for (origin in corsOrigins) {
			allowHost(origin, schemes = listOf("https"))
		}
		allowHeader(HttpHeaders.ContentType)
	}
	install(StatusPages) {
		val codes = generateErrorCodes().toTypedArray()

		val htmlUtf8ContentType = ContentType.Text.Html.withCharset(Charsets.UTF_8)

		status(*codes) { statusCode ->
			val headers = content.headers
			val currentContentType = headers[HttpHeaders.ContentType]
			var message =
				if (currentContentType != null && currentContentType.contains("text/plain")) {
					extractContent(content)
				} else {
					null
				}
			if (message == null) {
				message =
					when (statusCode.value) {
						400 -> I18N.strings.http_400.localized()
						401 -> I18N.strings.http_401.localized()
						403 -> I18N.strings.http_403.localized()
						404 -> I18N.strings.http_404.localized()
						405 -> I18N.strings.http_405.localized()
						415 -> I18N.strings.http_415.localized()
						500 -> I18N.strings.http_500.localized()
						else -> "no message"
					}
			}
			call.respondHtmlTemplate(ErrorTemplate(), statusCode) {
				errorTitle { +"Error" }
				headline { +statusCode.description }
				message { +message }
			}
			call.response.headers.append(HttpHeaders.ContentType, htmlUtf8ContentType.toString())
		}
	}

	install(ContentNegotiation) {
		json()
		xml()
	}
	routing {
		staticResources("/", "www") {
			enableAutoHeadResponse()
			default("index.html")
			cacheControl {
				listOf(CacheControl.NoStore(null))
			}
		}
		configuration()
	}
}

class HttpService {
	companion object {
		fun create(
			port: Int = 24727,
			host: String = "localhost",
			wait: Boolean = true,
			serverAgent: UserAgent? = null,
			corsOrigins: Set<String> = setOf("service.skidentity.de"),
			configuration: Routing.() -> Unit,
		) {
			embeddedServer(
				Netty,
				port = port, // This is the port on which Ktor is listening
				host = host,
			) {
				configureServer(
					port = port,
					host = host,
					corsOrigins = corsOrigins,
					serverAgent = serverAgent?.toHeaderValue(),
					configuration = configuration,
				)
			}.start(wait = wait)
		}
	}
}
