description = "android-lib"

plugins {
	id("openecard.android-multiplatform-conventions")
}

android {
	namespace = "org.openecard.clients.android.lib"
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
		val androidMain by getting {
			dependencies {
				api(project(":clients:android-common"))
			}
		}
	}
}

dependencies {
	constraints {
		val reason = "Newer versions will break our build because of modularization since v4."
		api("jakarta.xml.bind:jakarta.xml.bind-api") {
			version {
				strictly("3.0.1")
			}
			because(reason)
		}
		api("jakarta.xml.ws:jakarta.xml.ws-api") {
			version {
				strictly("3.0.1")
			}
			because(reason)
		}
		api("org.glassfish.jaxb:jaxb-runtime") {
			version {
				strictly("3.0.2-mobile")
			}
			because(reason)
		}
	}
}

configurations.api {
    exclude(group= "com.google.code.findbugs", module= "jsr305")
    exclude(group= "com.google.code.findbugs", module= "annotations")
}
