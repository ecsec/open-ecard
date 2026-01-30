import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.androidKmpLibrary)
	alias(libs.plugins.kotlinMultiplatform)
	alias(appLibs.plugins.composeMultiplatform)
	alias(appLibs.plugins.composeCompiler)
	alias(appLibs.plugins.composeHotReload)
	alias(libs.plugins.kotlinSerialization)
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
			implementation(compose.components.uiToolingPreview)
			implementation(appLibs.androidx.lifecycle.viewmodelCompose)
			implementation(appLibs.androidx.lifecycle.runtimeCompose)
			implementation(libs.ktor.client.nego)
			implementation(libs.ktor.client.logging)
			implementation(libs.ktor.serde.json)
			implementation(libs.ktor.serde.xml)
			implementation(libs.ktor.client.core)
			implementation(libs.kotlin.logging)

			implementation("org.openecard.addons:tr03124")
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
		androidMain.dependencies {
			implementation(compose.preview)
			implementation(appLibs.androidx.activity.compose)
			implementation(libs.ktor.client.android)
			implementation(appLibs.sfl4j.android)
			api("org.openecard.smartcard:pcsc-android")
		}

		iosMain.dependencies {
			implementation("org.openecard.utils:openssl-interop")
			implementation("org.openecard.smartcard:pcsc-ios")
			implementation(libs.ktor.client.darwin)
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
