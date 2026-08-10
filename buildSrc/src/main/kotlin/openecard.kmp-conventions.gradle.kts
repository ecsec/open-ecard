plugins {
	kotlin("multiplatform")
	id("openecard.ktlint-conventions")
	id("openecard.coverage-conventions")
	id("dev.mokkery")
	kotlin("plugin.serialization")
}

val javaToolchain = project.property("javaToolchain") as String
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}

	applyDefaultHierarchyTemplate()
}

val testHeapSize = project.property("testHeapSize") as String
tasks.withType<Test> {
	maxHeapSize = testHeapSize
	useJUnitPlatform { }
}
