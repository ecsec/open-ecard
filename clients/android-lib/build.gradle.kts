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
				api(project(":addons:cardlink"))
				implementation(libs.xerces.imp)
			}
		}
	}
}

configurations.api {
    exclude(group= "com.google.code.findbugs", module= "jsr305")
    exclude(group= "com.google.code.findbugs", module= "annotations")
}
