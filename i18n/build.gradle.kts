description = "i18n"

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
			dependencies {}
		}
		val jvmTest by getting {
			dependencies {}
		}
	}
}

// kover gets confused because no sources are in this project
kover {
	disable()
}
