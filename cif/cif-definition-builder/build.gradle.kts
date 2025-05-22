description = "Card info file definition builder"

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
				api(project(":cif:cif-dsl-api"))
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
