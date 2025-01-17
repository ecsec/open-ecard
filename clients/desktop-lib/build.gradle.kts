description = "desktop-lib"

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
				api(project(":clients:mobile-lib"))
				api(project(":ifd:scio-backend:pcsc"))
				api(project(":addons:cardlink"))
				api(project(":wsdef:jaxb-marshaller"))
				implementation(libs.xerces.imp)
			}
		}
	}
}
