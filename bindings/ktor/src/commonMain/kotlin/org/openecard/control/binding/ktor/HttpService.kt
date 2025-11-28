package org.openecard.control.binding.ktor

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.charset
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import org.openecard.i18n.I18N
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger { }

fun extractContent(content: OutgoingContent): String? =
	when (content) {
		is OutgoingContent.ContentWrapper -> {
			extractContent(content.delegate())
		}

		is TextContent -> {
			content.text
		}

		is OutgoingContent.ByteArrayContent -> {
			content.bytes().toString(content.contentType?.charset() ?: StandardCharsets.UTF_8)
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
			headers.append("X-XSS-Protection", "1")
			headers.append(
				"Content-Security-Policy",
				"default-src 'none'; script-src 'none'; style-src 'self'; img-src 'self'",
			)
			headers.append("X-Content-Type-Options", "nosniff")
			headers.append("X-Frame-Options", "SAMEORIGIN")
		}
	}

suspend fun ApplicationCall.respondOecHtmlError(
	status: HttpStatusCode,
	title: String = "Error",
	message: String,
) {
	val htmlUtf8ContentType = ContentType.Text.Html.withCharset(Charsets.UTF_8)
	respondHtmlTemplate(ErrorTemplate(), status) {
		errorTitle { +title }
		headline { +status.description }
		message { +message }
	}
	response.headers.append(HttpHeaders.ContentType, htmlUtf8ContentType.toString())
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

		status(*codes) { statusCode ->
			val currentContentType = content.contentType
			var message =
				if (currentContentType != null && currentContentType.match("text/plain")) {
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
			call.respondOecHtmlError(statusCode, message = message)
		}
	}

	install(ContentNegotiation) {
		json()
		xml()
	}
	routing {
		staticResources("/", "www") {
			enableAutoHeadResponse()
			cacheControl {
				listOf(CacheControl.NoStore(null))
			}
		}
		get("/") { call.respondRedirect("index.html") }
		configuration()
	}
}

class HttpService(
	private val ktorInst: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>,
) {
	fun stop(
		gracePeriodMillis: Long = 1000L,
		timeoutMillis: Long = 1000L,
	) {
		ktorInst.stop(gracePeriodMillis, timeoutMillis)
	}

	val port by lazy {
		runBlocking {
			ktorInst.application.engine
				.resolvedConnectors()
				.first()
				.port
		}
	}

	companion object {
		fun start(
			port: Int = 24727,
			host: String = "localhost",
			wait: Boolean = true,
			serverAgent: UserAgent? = null,
			corsOrigins: Set<String> = setOf("service.skidentity.de"),
			configuration: Routing.() -> Unit,
		): HttpService {
			val inst =
				embeddedServer(
					CIO,
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
			return HttpService(inst)
		}
	}
}
