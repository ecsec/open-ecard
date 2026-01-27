import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(appLibs.plugins.androidApplication)
	alias(appLibs.plugins.composeMultiplatform)
	alias(appLibs.plugins.composeCompiler)
	alias(appLibs.plugins.composeHotReload)
	alias(libs.plugins.kotlinSerialization)
}

kotlin {
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_25)
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

	jvm()

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
			implementation("org.openecard.smartcard:pcsc-android")
			implementation(appLibs.sfl4j.android)
		}

		iosMain.dependencies {
			implementation("org.openecard.utils:openssl-interop")
			implementation("org.openecard.smartcard:pcsc-ios")
			implementation(libs.ktor.client.darwin)
		}
	}
}
android {
	namespace = "org.openecard.demo"
	compileSdk =
		libs.versions.androidCompileSdk
			.get()
			.toInt()

	defaultConfig {
		applicationId = "org.openecard.demo"
		minSdk =
			libs.versions.androidMinSdk
				.get()
				.toInt()
		targetSdk =
			appLibs.versions.android.targetSdk
				.get()
				.toInt()
		versionCode = 1
		versionName = "1.0"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

dependencies {
	debugImplementation(compose.uiTooling)
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
