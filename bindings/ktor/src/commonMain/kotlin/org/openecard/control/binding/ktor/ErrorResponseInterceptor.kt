package org.openecard.control.binding.ktor

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.BeforeResponseTransform
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.JsonNull.content
import org.openecard.i18n.I18N
import java.io.StringWriter

private fun generateErrorCodes(): List<Int> {
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
	return result
}

data class TemplateError(
	val title: String,
	val headline: String,
	val message: String,
)

private val utf8ContentType = ContentType.Text.Html.withCharset(Charsets.UTF_8)

val ErrorResponseInterceptor =
	createApplicationPlugin("ErrorResponseInterceptor", {

		val freemarkerPluginConfig = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
		freemarkerPluginConfig.templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")

		val codes = generateErrorCodes()
		onCallRespond { call ->
			val response = call.response
			val statusCode = response.status()

			if (statusCode != null && codes.contains(statusCode.value)) {
				transformBody { data ->
					val headers = response.headers
					val currentContentType = headers[HttpHeaders.ContentType]
					val message =
						if (currentContentType != null && currentContentType.contains("text/plain")) {
							data.toString()
						} else {
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
					headers.append(HttpHeaders.ContentType, utf8ContentType.toString())

					val writer = StringWriter()
					freemarkerPluginConfig.getTemplate("error.ftl").process(
						mapOf(
							"error" to
								TemplateError(
									title = "Error",
									headline = statusCode.description,
									message = message,
								),
						),
						writer,
					)

					val result = TextContent(text = writer.toString(), utf8ContentType)

					result
				}
			}
		}

		@OptIn(InternalAPI::class)
		on(BeforeResponseTransform(FreeMarkerContent::class)) { _, content ->
			with(content) {
			}
		}
	})
