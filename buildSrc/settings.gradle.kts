dependencyResolutionManagement {
	repositories {
		google()
		gradlePluginPortal()
		mavenCentral()
	}

	versionCatalogs {
		create("libs") {
			from(files("../libs.versions.toml"))
		}
	}
}
