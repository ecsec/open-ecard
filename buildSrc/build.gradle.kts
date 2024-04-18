import org.jetbrains.kotlin.konan.properties.loadProperties

val propsFile = buildFile.parentFile.parentFile.resolve("gradle.properties")
val props = loadProperties(propsFile.absolutePath)


val javaToolchain: String by props
java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaToolchain))
	}
}


repositories {
	gradlePluginPortal()
	google()
	mavenCentral()
}

plugins {
	// Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
	`kotlin-dsl`
}


val androidPluginVersion: String by props
val androidSdkResolverVersion: String by props

//val kotlinPluginVersion = "1.9.20"
dependencies {
//	implementation("org.jetbrains.kotlin.jvm", "org.jetbrains.kotlin.jvm.gradle.plugin", kotlinPluginVersion)
//	implementation("org.jetbrains.kotlin.plugin.allopen", "org.jetbrains.kotlin.plugin.allopen.gradle.plugin", kotlinPluginVersion )
	implementation("com.android.library:com.android.library.gradle.plugin:${androidPluginVersion}")
	implementation("com.quittle:setup-android-sdk:${androidSdkResolverVersion}")
}
