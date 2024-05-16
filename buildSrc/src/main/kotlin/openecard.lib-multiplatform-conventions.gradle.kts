plugins {
	kotlin("multiplatform")
	id("openecard.publish-conventions")
	id("openecard.coverage-conventions")
}

val javaToolchain: String by project
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}
	//java.targetCompatibility = JavaVersion.valueOf("11")

	applyDefaultHierarchyTemplate()

	jvm {
		withJava()
	}
}

val testHeapSize: String by project
tasks.withType<Test> {
	maxHeapSize = testHeapSize
	useTestNG()
}

// set encoding for legacy java tasks, remove once all java is migrated to kotlin
tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}
tasks.withType<Javadoc> {
	options.encoding = "UTF-8"
}
