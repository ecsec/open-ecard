plugins {
	kotlin("multiplatform")
	id("org.jlleitschuh.gradle.ktlint")
}

val javaToolchain: String by project
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}

	applyDefaultHierarchyTemplate()
}

// configure ktlint
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
	version =
		versionCatalogs
			.find("libs")
			.get()
			.findVersion("ktlint")
			.get()
			.requiredVersion

	reporters {
		customReporters {
			register("gitlab") {
				fileExtension = "json"
				dependency =
					versionCatalogs
						.find("libs")
						.get()
						.findLibrary("ktlint.githubreporter")
						.get()
						.get()
			}
		}
	}
}
