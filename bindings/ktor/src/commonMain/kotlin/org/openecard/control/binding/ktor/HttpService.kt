package org.openecard.control.binding.ktor

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HeaderValue
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.charset
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.CORSConfig
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.acceptItems
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.RootRouteSelector
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

private val defaultErrorCodes: List<HttpStatusCode> by lazy {
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
	result.map { HttpStatusCode.fromValue(it) }
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
	createRouteScopedPlugin("CacheControlHeader") {
		onCall { call ->
			call.response.headers.append("Cache-Control", "no-store")
		}
	}

val SecurityHeader =
	createRouteScopedPlugin("SecurityHeader") {
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

val RestrictToFetch =
	createRouteScopedPlugin("RestrictToFetch") {
		onCall { call ->
			val mode = call.request.header("Sec-Fetch-Mode")
			if (mode != null && mode !in setOf("cors", "navigate")) {
				call.respond(HttpStatusCode.Forbidden)
			}
			val dest = call.request.header("Sec-Fetch-Dest")
			if (dest != null && dest !in setOf("document", "empty")) {
				call.respond(HttpStatusCode.Forbidden)
			}
		}
	}

val RestrictToNavigation =
	createRouteScopedPlugin("RestrictToNavigation") {
		onCall { call ->
			val mode = call.request.header("Sec-Fetch-Mode")
			if (mode != null && mode !in setOf("navigate")) {
				call.respond(HttpStatusCode.Forbidden)
			}
			val dest = call.request.header("Sec-Fetch-Dest")
			if (dest != null && dest !in setOf("document", "frame", "iframe", "fencedframe")) {
				call.respond(HttpStatusCode.Forbidden)
			}
		}
	}

val RestrictToSameSite =
	createRouteScopedPlugin("RestrictToSameSite") {
		onCall { call ->
			val site = call.request.header("Sec-Fetch-Site")
			if (site != null && site !in setOf("none", "same-site", "same-origin")) {
				call.respond(HttpStatusCode.Forbidden)
			}
		}
	}

fun CORSConfig.addAllowedHosts(corsOrigins: Set<Url>) {
	for (origin in corsOrigins) {
		allowHost("${origin.host}:${origin.port}", schemes = listOf(origin.protocol.name))
	}
	allowHeader(HttpHeaders.ContentType)
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

internal fun Application.configureServer(
	serverAgent: String?,
	configuration: Routing.() -> Unit,
) {
	if (serverAgent != null) {
		install(ServerHeader) {
			header = serverAgent
		}
	}

	install(StatusPages) {
		status(*defaultErrorCodes.toTypedArray()) { statusCode ->
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

			for (nextMimeType in call.request.acceptItems() + HeaderValue("*/*")) {
				if (ContentType.Text.Html.match(nextMimeType.value)) {
					call.respondOecHtmlError(statusCode, message = message)
					break
				} else if (ContentType.Text.Plain.match(nextMimeType.value)) {
					call.respondText(ContentType.Text.Plain, statusCode) { message }
					break
				} else if (ContentType.Application.Json.match(nextMimeType.value)) {
					call.respond(statusCode, mapOf("error" to message))
				}
			}
		}
	}

	install(ContentNegotiation) {
		json()
		xml()
	}
	install(SecurityHeader)

	routing {
		get("/") {
			call.respondRedirect("/static/")
		}
		createChild(RootRouteSelector("/static")).apply {
			install(RestrictToSameSite)

			staticResources("/", "www", "index.html") {
				enableAutoHeadResponse()
				cacheControl {
					listOf(CacheControl.NoStore(null))
				}
			}
		}
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
			configuration: Routing.() -> Unit,
		): HttpService {
			val inst =
				embeddedServer(
					CIO,
					port = port, // This is the port on which Ktor is listening
					host = host,
				) {
					configureServer(
						serverAgent = serverAgent?.toHeaderValue(),
						configuration = configuration,
					)
				}.start(wait = wait)
			return HttpService(inst)
		}
	}
}
