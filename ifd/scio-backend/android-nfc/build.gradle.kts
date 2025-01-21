description = "android-nfc"

plugins {
	id("openecard.android-multiplatform-conventions")
}

android {
	namespace = "org.openecard.ifd.nfc"
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
		val androidMain by getting {
			dependencies {
				api(project(":ifd:scio-backend:mobile-nfc"))
			}
		}
	}
}
