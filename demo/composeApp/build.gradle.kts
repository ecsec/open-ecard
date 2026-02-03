import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(appLibs.plugins.androidKmpLibrary)
	alias(appLibs.plugins.kotlinMultiplatform)
	alias(appLibs.plugins.composeMultiplatform)
	alias(appLibs.plugins.composeCompiler)
	alias(appLibs.plugins.composeHotReload)
	alias(appLibs.plugins.kotlinSerialization)
}

kotlin {
	android {
		compileSdk { version = release(Integer.parseInt(appLibs.versions.androidCompileSdk.get())) }
	}

	androidLibrary {
		namespace = "org.openecard.demo.composedemolibrary"
		compilerOptions {
			jvmTarget.set(JvmTarget.fromTarget(appLibs.versions.javaTarget.get()))
		}
		androidResources {
			enable = true
		}
	}

	listOf(
		iosArm64(),
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "ComposeApp"
			isStatic = true
		}
	}

	sourceSets {
		commonMain.dependencies {
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			implementation(compose.ui)
			implementation(compose.components.resources)
			implementation(compose.materialIconsExtended)
			implementation(compose.components.uiToolingPreview)
			implementation(appLibs.androidx.lifecycle.viewmodelCompose)
			implementation(appLibs.androidx.lifecycle.runtimeCompose)
			implementation(appLibs.composeNavigation)
			implementation(appLibs.ktor.client.nego)
			implementation(appLibs.ktor.client.logging)
			implementation(appLibs.ktor.serde.json)
			implementation(appLibs.ktor.serde.xml)
			implementation(appLibs.ktor.client.core)
			implementation(appLibs.kotlin.logging)

			implementation(appLibs.fleeksoft.charset)
			implementation(appLibs.okio)


			implementation("org.openecard.addons:tr03124")
		}
		commonTest.dependencies {
			implementation(appLibs.kotlin.test)
		}
		androidMain.dependencies {
			implementation(compose.preview)
			implementation(appLibs.androidx.activity.compose)
			implementation(appLibs.ktor.client.android)
			implementation(appLibs.sfl4j.android)
			api("org.openecard.smartcard:pcsc-android")
		}

		iosMain.dependencies {
			implementation("org.openecard.utils:openssl-interop")
			implementation("org.openecard.smartcard:pcsc-ios")
			implementation(appLibs.ktor.client.darwin)
		}
	}
}

compose.desktop {
	application {
		mainClass = "org.openecard.demo.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "org.openecard.demo"
			packageVersion = "1.0.0"
		}
	}
}
