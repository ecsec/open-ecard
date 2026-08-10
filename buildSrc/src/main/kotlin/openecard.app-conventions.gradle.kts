plugins {
	application
	kotlin("jvm")
	id("openecard.ktlint-conventions")
	id("dev.mokkery")
}

val javaToolchain = project.property("javaToolchain") as String
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}
}

val testHeapSize = project.property("testHeapSize") as String
tasks {
	test {
		maxHeapSize = testHeapSize
		useJUnitPlatform { }
	}
}
