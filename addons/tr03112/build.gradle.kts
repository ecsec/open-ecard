description = "tr03112"

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
				api(project(":crypto:tls"))
				api(project(":ifd:ifd-common"))
				api(project(":sal:sal-common"))
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics)
				implementation(project(":sal:tiny-sal"))
			}
		}
	}
}
