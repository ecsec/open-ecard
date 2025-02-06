description = "jaxb-marshaller"

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
				implementation(libs.jaxb.impl)
				implementation(project(":wsdef:wsdef-common"))
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(project(":wsdef:wsdef-client"))
			}
		}
	}
}
