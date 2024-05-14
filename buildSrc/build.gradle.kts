import org.jetbrains.kotlin.konan.properties.loadProperties

val propsFile = buildFile.parentFile.parentFile.resolve("gradle.properties")
val props = loadProperties(propsFile.absolutePath)


val javaToolchain: String by props
java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaToolchain))
	}
}


plugins {
	// Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
	`kotlin-dsl`
}


dependencies {
	implementation(libs.plugins.kotlinJvm)
	implementation(libs.plugins.kotlinKapt)
	implementation(libs.plugins.kotlinAllOpen)
	implementation(libs.plugins.kotlinMultiplatform)
	implementation(libs.plugins.kotlinKover)

	implementation(libs.plugins.androidLibrary)
	implementation(libs.robovm.gradlePlugin)
}

fun DependencyHandlerScope.implementation(pluginProv: Provider<PluginDependency>) {
	pluginProv.orNull ?.let { plugin ->
		val pluginId = plugin.pluginId
		val pluginVer = plugin.version
		this.implementation("$pluginId:$pluginId.gradle.plugin") {
			version {
				branch = pluginVer.branch
				prefer(pluginVer.preferredVersion)
				if (pluginVer.requiredVersion.isNotBlank()) {
					require(pluginVer.requiredVersion)
				} else {
					strictly(pluginVer.strictVersion)
				}
			}
		}
	}
}
