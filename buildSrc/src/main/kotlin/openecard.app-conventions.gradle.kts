plugins {
	application
	kotlin("jvm")
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
