plugins {
	kotlin("multiplatform")
	id("org.jlleitschuh.gradle.ktlint")
	// TODO: reactivate this when https://github.com/Kotlin/kotlinx-kover/issues/747 is solved
	// id("openecard.coverage-conventions")
	id("dev.mokkery")
	kotlin("plugin.serialization")
}

val javaToolchain: String by project
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}

	applyDefaultHierarchyTemplate()
}

val testHeapSize: String by project
tasks.withType<Test> {
	maxHeapSize = testHeapSize
	useJUnitPlatform { }
}

// configure ktlint
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
	ignoreFailures = project.findProperty("ktlint.ignoreFailures")?.toString().toBoolean()
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

	filter {
		// don't check generated sources
		exclude {
			val generatedRoot =
				layout.buildDirectory
					.dir("generated")
					.get()
					.asFile
			it.file.startsWith(generatedRoot)
		}
	}
}
