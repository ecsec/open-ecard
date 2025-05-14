description = "ios-lib"

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

				api(project(":clients:ios-common"))
				api(project(":wsdef:jaxb-marshaller"))
				api(libs.httpcore)
				api(project(":addons:cardlink"))

				implementation(libs.xerces.imp)

				implementation(libs.annotations)
			}
		}
		val jvmTest by getting {
			dependencies {
			}
		}
	}
}
