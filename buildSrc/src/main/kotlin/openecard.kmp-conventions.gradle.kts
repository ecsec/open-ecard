plugins {
	kotlin("multiplatform")
}

val javaToolchain: String by project
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}

	applyDefaultHierarchyTemplate()
}
