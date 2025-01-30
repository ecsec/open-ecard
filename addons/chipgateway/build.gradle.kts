description = "chipgateway"

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
				api(libs.bc.pkix)
				api(libs.jackson.jaxb)
				api(libs.jose4j)

				implementation(libs.annotations)
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(project(":gui:swing"))
			}
		}
	}
}
