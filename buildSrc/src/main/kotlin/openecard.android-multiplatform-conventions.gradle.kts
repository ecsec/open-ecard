plugins {
	kotlin("multiplatform")
	id("openecard.android-conventions")
}

val javaToolchain: String by project
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}

	applyDefaultHierarchyTemplate()

	androidTarget {  }

	jvm {  }
}

val testHeapSize: String by project
tasks.withType<Test> {
	maxHeapSize = testHeapSize
	useTestNG()
}
