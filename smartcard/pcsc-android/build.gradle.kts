description = "Smartcard PCSC Implementation for android"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-android-lib-conventions")
}

kotlin {

	sourceSets {
		androidMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":smartcard:sc-base"))
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}

		androidDeviceTest.dependencies {
			implementation(libs.bundles.test.android.kotlin)
		}
	}
}
