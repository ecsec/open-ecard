description = "common"

plugins {
	id("openecard.lib-multiplatform-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlin.logging)
				implementation(kotlin("reflect"))
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
				api(libs.bc.oec.prov)
				api(libs.bc.oec.tls)
				api(libs.httpcore)

				implementation(libs.annotations)
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.jvm.kotlin)
				implementation(libs.bundles.test.mockito)
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
