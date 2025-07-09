description = "localhost HTTP Binding"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	// id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.ktor.server.core)
				implementation(libs.ktor.server.cio)
				implementation(libs.ktor.server.netty)
				implementation(libs.ktor.server.cors)
				implementation(libs.ktor.server.content.negotiation)
				implementation(libs.ktor.server.freemarker)
				implementation(libs.ktor.server.status.pages)
				implementation(libs.ktor.serde.json)
				implementation(libs.ktor.serde.xml)
				implementation(libs.kotlin.logging)

				api(project(":common"))
				api(project(":i18n"))
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
				implementation(libs.ktor.server.test.host)
			}
		}
		val jvmMain by getting {
			dependencies {
			}
		}
		val jvmTest by getting {
			dependencies {
			}
		}
	}
}
