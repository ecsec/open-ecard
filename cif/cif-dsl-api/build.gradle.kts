description = "DSL API for Card Info Files"

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
			api(project(":cif:cif-definition"))
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
