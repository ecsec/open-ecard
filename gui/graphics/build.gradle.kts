description = "graphics"

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
				api(libs.slf4j.api)
				api(libs.apache.batik)
			}
		}
		val jvmTest by getting {
			dependencies {
			}
		}
	}
}
