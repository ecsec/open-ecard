val androidCompileSdk: String by project
val androidMinSdk: String by project

plugins {
	id("openecard.kmp-conventions")
	id("openecard.ktlint-conventions")

	// alias(libs.plugins.kotlinMultiplatform)
	// alias(libs.plugins.androidApplication)
	// alias(libs.plugins.composeMultiplatform)
	// alias(libs.plugins.composeCompiler)
	// alias(libs.plugins.composeHotReload)
	id("com.android.application")
	id("dev.mokkery")
}
kotlin {
	androidTarget { }
}

android {
	compileSdk = androidCompileSdk.toInt()
	defaultConfig {
		minSdk = androidMinSdk.toInt()
		targetSdk = androidMinSdk.toInt()
	}
}
