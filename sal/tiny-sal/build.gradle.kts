description = "tiny-sal"

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
				api(project(":sal:sal-common"))
				api(project(":cifs"))
				api(project(":i18n"))
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(project(":wsdef:jaxb-marshaller"))
				implementation(project(":gui:swing"))
				implementation(project(":ifd:ifd-core"))
			}
		}
	}
}
