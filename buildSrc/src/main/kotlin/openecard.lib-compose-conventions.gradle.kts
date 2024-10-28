plugins {
	kotlin("multiplatform")
	id("org.jetbrains.compose")
	id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(compose.components.resources)
				implementation(compose.runtime)
			}
		}
	}
}
