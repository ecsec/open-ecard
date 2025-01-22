description = "sal-common"

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
				api(project(":common"))
				api(project(":addon"))
				api(project(":crypto:crypto-common"))

				implementation(libs.annotations)
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics)
				implementation(project(":wsdef:jaxb-marshaller"))
				implementation(project(":cifs"))
			}
		}
	}
}
