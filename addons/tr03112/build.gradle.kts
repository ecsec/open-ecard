description = "tr03112"

plugins {
	id("openecard.lib-multiplatform-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				api(project(":common"))
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
				api(project(":crypto:tls"))
				api(project(":ifd:ifd-common"))
				api(project(":sal:sal-common"))
				implementation(project(":ifd:ifd-protocols:pace"))

				implementation(libs.annotations)
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
