plugins {
	kotlin("multiplatform")
}

val javaToolchain: String by project
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}
	//java.targetCompatibility = JavaVersion.valueOf("11")

	applyDefaultHierarchyTemplate()

	jvm {  }
}

val testHeapSize: String by project
tasks.withType<Test> {
	maxHeapSize = testHeapSize
}
