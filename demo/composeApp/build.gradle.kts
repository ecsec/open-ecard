import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.composeHotReload)
}

kotlin {
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
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
			implementation(libs.androidx.lifecycle.viewmodelCompose)
			implementation(libs.androidx.lifecycle.runtimeCompose)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
         androidMain.dependencies {
             implementation(compose.preview)
             implementation(libs.androidx.activity.compose)
         }

		iosMain.dependencies {
			implementation(libs.oec.utils.openssl)
			implementation(libs.oec.addons.transport)
		}
	}
}
android {
	namespace = "org.openecard.demo"
	compileSdk =
		libs.versions.android.compileSdk
			.get()
			.toInt()

	defaultConfig {
		applicationId = "org.openecard.demo"
		minSdk =
			libs.versions.android.minSdk
				.get()
				.toInt()
		targetSdk =
			libs.versions.android.targetSdk
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
