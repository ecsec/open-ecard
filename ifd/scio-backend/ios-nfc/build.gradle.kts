description = "ios-nfc"

plugins {
	id("openecard.lib-multiplatform-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlin.logging)
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
			}
		}
		val jvmMain by getting {
			dependencies {
				implementation(libs.robovm.rt)
				implementation(libs.robovm.cocoa)
				api(project(":ifd:scio-backend:mobile-nfc"))
			}
		}
		val jvmTest by getting {
			dependencies {
			}
		}
	}
}
