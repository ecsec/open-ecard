import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

actual fun configureLog() {
	KotlinLoggingConfiguration.direct.logLevel = Level.DEBUG
}
