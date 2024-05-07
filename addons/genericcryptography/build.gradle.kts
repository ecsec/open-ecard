description = "genericcryptography"

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
				api(project(":addon"))
				api(project(":sal:sal-common"))
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(project(":gui:swing"))
				implementation(project(":ifd:ifd-core"))
				implementation(project(":sal:tiny-sal"))
			}
		}
	}
}
