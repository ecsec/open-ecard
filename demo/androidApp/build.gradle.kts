
plugins {
// 	alias(libs.plugins.kotlinMultiplatform)
// 	alias(libs.plugins.androidKmpLibrary)
	alias(appLibs.plugins.androidApplication)
// 	alias(appLibs.plugins.kotlinAndroid)
	alias(appLibs.plugins.composeMultiplatform)
	alias(appLibs.plugins.composeCompiler)
	alias(appLibs.plugins.composeHotReload)
	alias(libs.plugins.kotlinSerialization)
}

kotlin {
	dependencies {
		implementation(appLibs.androidx.activity.compose)
		implementation(libs.ktor.client.android)
		implementation(appLibs.sfl4j.android)
		implementation(projects.composeApp)
	}
	/*
	target {
		compilerOptions {
			// jvmTarget.set(JvmTarget.JVM_25)
		}
	}

	 */
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
