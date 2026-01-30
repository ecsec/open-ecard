
plugins {
	alias(appLibs.plugins.androidApplication)
	alias(appLibs.plugins.composeMultiplatform)
	alias(appLibs.plugins.composeCompiler)
	alias(appLibs.plugins.composeHotReload)
	alias(appLibs.plugins.kotlinSerialization)
}

kotlin {
	dependencies {
		implementation(appLibs.androidx.activity.compose)
		implementation(compose.components.uiToolingPreview)
		implementation(appLibs.ktor.client.android)
		implementation(appLibs.sfl4j.android)
		implementation(projects.composeApp)
	}
}

android {

	namespace = "org.openecard.demo"
	compileSdk =
		appLibs.versions.androidCompileSdk
			.get()
			.toInt()

	defaultConfig {
		applicationId = "org.openecard.demo"
		minSdk =
			appLibs.versions.androidMinSdk
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
