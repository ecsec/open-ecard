description = "swing"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlin.logging)
				implementation(project(":utils:common"))
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
			}
		}
		val jvmMain by getting {
			dependencies {
				api(project(":gui:graphics"))
				api(project(":i18n"))
				api(libs.pdfbox)
				implementation(libs.ktor.client.core)
			}
		}
		val jvmTest by getting {
			dependencies {
			}
		}
	}
}
