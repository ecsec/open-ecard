description = "common"

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
				api(libs.slf4j.api)
				api(project(":wsdef:wsdef-common"))
				api(project(":wsdef:wsdef-client"))
				implementation(project(":i18n"))
				api(libs.bc.prov)
				api(libs.bc.tls)
				api(libs.httpcore)

				implementation(libs.annotations)
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(project(":wsdef:jaxb-marshaller"))
			}
		}
	}
}

tasks.named("jvmProcessResources", ProcessResources::class).configure {
	filesMatching("**/VERSION") {
		expand("version" to project.version.toString())
	}
}
