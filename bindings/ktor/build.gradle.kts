description = "localhost HTTP Binding"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	// id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			api(libs.ktor.server.core)
			implementation(libs.ktor.server.cio)
			api(libs.ktor.server.cors)
			implementation(libs.ktor.server.content.negotiation)
			implementation(libs.ktor.server.html.builder)
			implementation(libs.ktor.server.status.pages)
			implementation(libs.ktor.serde.json)
			implementation(libs.ktor.serde.xml)
			implementation(libs.kotlin.logging)

			api(project(":i18n"))
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(libs.ktor.server.test.host)
		}

		jvmMain.dependencies {
		}

		jvmTest.dependencies {
		}
	}
}
