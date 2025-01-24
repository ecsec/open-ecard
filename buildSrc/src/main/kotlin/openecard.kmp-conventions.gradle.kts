import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
	kotlin("multiplatform")
}

val javaToolchain: String by project
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(javaToolchain)
	}

	applyDefaultHierarchyTemplate()

	compilerOptions {
		this.languageVersion = KotlinVersion.KOTLIN_2_0
	}
}
