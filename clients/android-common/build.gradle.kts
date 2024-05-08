description = "android-common"

plugins {
	id("openecard.android-multiplatform-conventions")
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
				api(project(":ifd:scio-backend:android-nfc"))
				api(project(":clients:mobile-lib"))
				api(project(":wsdef:jaxb-marshaller"))
			}
		}
	}
}

android {
	namespace = "org.openecard.clients.android.common"
}
