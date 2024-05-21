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
				//	api(libs.jaxb.api)
				//	api(libs.jaxb.ws.api)
				//	api(libs.slf4j.api)
				api(project(":addon"))
				api(project(":crypto:tls"))
				api(project(":ifd:ifd-common"))
				api(project(":sal:sal-common"))
				api(project(":clients:mobile-lib"))
				implementation(project(":addons:tr03112"))
				//	implementation(project(":i18n"))
				//	api(libs.bc.prov)
				//	api(libs.bc.tls)
				//	api(libs.httpcore)

				implementation(libs.kotlin.serialization.json)

				//	testImplementation(libs.bundles.test.basics)
				//	testImplementation(project(":gui:swing"))
				//	testImplementation(project(":ifd:ifd-core"))
				//	testImplementation(project(":management"))
				//	testImplementation(project(":sal:tiny-sal"))
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics)
				implementation(libs.logback.classic)
				implementation(libs.kotlin.logging)
				implementation(project(":ifd:scio-backend:pcsc"))
				implementation(project(":wsdef:wsdef-common"))
				implementation(project(":wsdef:jaxb-marshaller"))
			}
		}
	}
}
