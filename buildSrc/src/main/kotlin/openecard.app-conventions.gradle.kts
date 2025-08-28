plugins {
	application
	kotlin("jvm")
	id("openecard.ktlint-conventions")
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
		useTestNG()
	}
}
