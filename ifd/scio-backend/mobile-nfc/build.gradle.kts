description = "mobile-nfc"

plugins {
	id("openecard.lib-multiplatform-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlin.logging)
				api(project(":ifd:ifd-common"))
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
			}
		}
		val jvmMain by getting {
			dependencies {
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.awaitility)
			}
		}
	}
}
