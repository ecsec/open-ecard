description = "Model for Card Info File definitions"

plugins {
	id("openecard.lib-multiplatform-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlin.logging)
				api(project(":utils:serialization"))
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
