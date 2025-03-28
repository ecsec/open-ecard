description = "releases"

plugins {
	id("openecard.lib-multiplatform-conventions")
	id("openecard.moko-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlin.logging)

				api(libs.semver)

				api(libs.kotlin.serialization.json)

				api(libs.ktor.client.core)
				implementation(libs.ktor.client.logging)

				implementation(libs.jwtkt.core)
				implementation(libs.jwtkt.ecdsa)
				implementation(libs.kotlinx.datetime)

				implementation(libs.moko.resources)
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
				implementation(libs.moko.resourcesTest)
			}
		}
		val jvmMain by getting {
			dependencies {
				implementation(libs.ktor.client.cio)
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.wiremock)
			}
		}
	}
}
