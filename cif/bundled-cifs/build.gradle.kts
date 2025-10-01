description = "Bundled CIFs"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
	id("openecard.kmp-desktop-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			api(project(":cif:cif-definition-builder"))
			implementation(project(":utils:common"))
		}
		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(libs.kotlin.serialization.json)
		}
		jvmMain.dependencies {
		}
		jvmTest.dependencies {
		}
	}
}
