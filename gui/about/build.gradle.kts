description = "about"

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
				api(project(":gui:graphics"))
				api(project(":gui:swing"))
			}
		}
		val jvmTest by getting {
			dependencies {}
		}
	}
}
