description = "tls"

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
				api(project(":crypto:crypto-common"))
				api(libs.bc.oec.prov)
				api(libs.bc.oec.tls)
				api(libs.proxyvole)
			}
		}
		val jvmTest by getting {
			dependencies {
			}
		}
	}
}
