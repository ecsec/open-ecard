description = "DSL API for Card Info Files"

plugins {
	id("openecard.lib-multiplatform-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlin.logging)
				api(project(":utils:serialization"))
				api(project(":cif:cif-definition"))
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
				implementation(project(":utils:common"))
			}
		}
		val jvmMain by getting {
			dependencies {}
		}
		val jvmTest by getting {
			dependencies {
			}
		}
	}
}
