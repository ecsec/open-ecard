description = "CardLink plugin"

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
				implementation(libs.logback.classic)
			}
		}
		val jvmMain by getting {
			dependencies {
				api(project(":addon"))
				api(project(":crypto:tls"))
				api(project(":ifd:ifd-common"))
				api(project(":sal:sal-common"))
				api(project(":clients:mobile-lib"))
				implementation(project(":addons:tr03112"))

				implementation(libs.kotlin.serialization.json)
				implementation(libs.kotlin.coroutines.core)
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.mockito)
				implementation(project(":ifd:scio-backend:pcsc"))
				implementation(project(":wsdef:wsdef-common"))
				implementation(project(":wsdef:jaxb-marshaller"))
			}
		}
	}
}
