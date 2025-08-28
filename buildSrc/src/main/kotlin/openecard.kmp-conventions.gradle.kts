plugins {
	kotlin("multiplatform")
	id("openecard.ktlint-conventions")
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
