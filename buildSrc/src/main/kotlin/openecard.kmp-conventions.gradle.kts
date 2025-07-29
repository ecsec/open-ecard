plugins {
	kotlin("multiplatform")
	id("openecard.ktlint-conventions")
	id("openecard.coverage-conventions")
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
