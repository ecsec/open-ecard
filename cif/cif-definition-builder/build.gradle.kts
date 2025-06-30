description = "Card info file definition builder"

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
			api(project(":cif:cif-dsl-api"))
			implementation(project(":utils:common"))
		}
		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}
		jvmMain.dependencies {
		}
		jvmTest.dependencies {
		}
	}
}
