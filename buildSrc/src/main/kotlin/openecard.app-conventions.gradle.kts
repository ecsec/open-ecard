plugins {
	application
	kotlin("jvm")
	id("openecard.ktlint-conventions")
	id("dev.mokkery")
}

val javaToolchain: String by project
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}
}

val testHeapSize: String by project
tasks {
	test {
		maxHeapSize = testHeapSize
		useJUnitPlatform { }
	}
}
