description = "middleware-sal"

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
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics)
			}
		}
	}
}
