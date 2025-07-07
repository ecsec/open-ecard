package org.openecard.control.binding.ktor

import freemarker.cache.ClassTemplateLoader
import io.ktor.http.CacheControl
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import org.openecard.common.AppVersion

val ServerHeader =
	createApplicationPlugin("ServerHeader") {

		/**
		 * Creates the value of the `Server` header according to BSI-TR-03124-1 v1.2 section 2.2.2.1.
		 */
		val builder = StringBuilder()

		builder.append(AppVersion.name)
		builder.append("/")
		builder.append(AppVersion.version)

		builder.append(" (")
		var firstSpec = true
		for (version in AppVersion.specVersions) {
			if (!firstSpec) {
				builder.append(" ")
			} else {
				firstSpec = false
			}
			builder.append(AppVersion.specName)
			builder.append("/")
			builder.append(version)
		}
		builder.append(")")

		val headerValue = builder.toString()

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
	configuration: Routing.() -> Unit,
) {
	install(ErrorResponseInterceptor)
	install(ServerHeader)
	install(SecurityHeader)
	install(CacheControlHeader)
	install(CORS) {
		allowHost("$host:$port")
		for (origin in corsOrigins) {
			allowHost(origin, schemes = listOf("https"))
		}
		allowHeader(HttpHeaders.ContentType)
	}
	install(ContentNegotiation) {
		json()
		xml()
	}
	install(FreeMarker) {
		templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
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
			host: String = "0.0.0.0",
			wait: Boolean = true,
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
					configuration = configuration,
				)
			}.start(wait = wait)
		}
	}
}
