plugins {
	// this is necessary to avoid the plugins to be loaded multiple times
	// in each subproject's classloader

	alias(appLibs.plugins.kotlinAndroid) apply false
	alias(libs.plugins.androidKmpLibrary) apply false
	alias(appLibs.plugins.androidLibrary) apply false

	alias(appLibs.plugins.androidApplication) apply false
	alias(appLibs.plugins.composeHotReload) apply false
	alias(appLibs.plugins.composeMultiplatform) apply false
	alias(appLibs.plugins.composeCompiler) apply false
	alias(libs.plugins.kotlinMultiplatform) apply false
	alias(libs.plugins.kotlinSerialization) apply false
}
