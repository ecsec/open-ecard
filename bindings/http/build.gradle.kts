description = "http"

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
				// 	api(project(":wsdef:wsdef-common"))
				// 	api(project(":wsdef:wsdef-client"))
				// 	implementation(project(":i18n"))
				// 	api(libs.bc.prov)
				// 	api(libs.bc.tls)
				// 	api(libs.httpcore)
				api(project(":addon"))
				api(project(":i18n"))

				implementation(libs.annotations)
			}
		}
		val jvmTest by getting {
			dependencies {
				// implementation(libs.bundles.test.powermock)
				implementation(project(":gui:swing"))
				implementation(project(":ifd:ifd-core"))
				implementation(project(":management"))
				implementation(project(":sal:tiny-sal"))
			}
		}
	}
}

tasks.named("jvmProcessResources", ProcessResources::class).configure {
	doLast {
		// build file listing of www files in resources dir, separated with :
		val files =
			destinationDir
				.resolve("www")
				.walk()
				.filter { it.isFile }
				.map { it.relativeTo(destinationDir).path }
				.map { "/$it" }
				.joinToString(":")
		destinationDir.resolve("www-files").writeText(files)
	}
}
