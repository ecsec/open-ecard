plugins {
	// this is necessary to avoid the plugins to be loaded multiple times
	// in each subproject's classloader

	alias(appLibs.plugins.androidApplication) apply false
	alias(appLibs.plugins.androidKmpLibrary) apply false
	alias(appLibs.plugins.kotlinMultiplatform) apply false
	alias(appLibs.plugins.composeMultiplatform) apply false
	alias(appLibs.plugins.composeCompiler) apply false
	alias(appLibs.plugins.composeHotReload) apply false
	alias(appLibs.plugins.kotlinSerialization) apply false
}
