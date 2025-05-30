description = "Model for Card Info File definitions"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":utils:serialization"))
		}
		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(project(":utils:common"))
		}
		jvmMain.dependencies {
		}
		jvmTest.dependencies {
		}
	}
}
