description = "pin-management"

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
				api(project(":i18n"))
				api(project(":ifd:ifd-core"))
				api(project(":sal:sal-common"))
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.jvm.kotlin)
				implementation(project(":sal:tiny-sal"))
			}
		}
	}
}
