description = "ifd-core"

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
				api(project(":ifd:ifd-common"))
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics)
				implementation(project(":gui:swing"))
			}
		}
	}
}
