description = "mobile-lib"

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
				compileOnly(libs.robovm.rt)
				compileOnly(libs.robovm.cocoa)
				compileOnly(libs.roboface.annots)
				implementation(libs.roboface.marshal)

				api(project(":management"))
				api(project(":sal:tiny-sal"))
				api(project(":addons:tr03112"))
				api(project(":addons:pin-management"))
				api(project(":addons:status"))
				api(project(":addons:genericcryptography"))
				api(project(":ifd:ifd-protocols:pace"))

				implementation(libs.annotations)
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(project(":ifd:scio-backend:mobile-nfc"))
				implementation(project(":wsdef:jaxb-marshaller"))
			}
		}
	}
}
