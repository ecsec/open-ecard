description = "addon"

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
				// 	api(libs.jaxb.api)
				// 	api(libs.jaxb.ws.api)
				// 	api(libs.slf4j.api)
				api(project(":common"))
				// 	api(project(":wsdef:wsdef-client"))
				// 	implementation(project(":i18n"))
				// 	api(libs.bc.oec.prov)
				// 	api(libs.bc.oec.tls)
				// 	api(libs.httpcore)
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.jvm.kotlin)
				implementation(project(":wsdef:jaxb-marshaller"))
			}
		}
	}
}
