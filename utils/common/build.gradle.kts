description = "Common utilities"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
	id("openecard.kmp-desktop-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}

		nativeMain.dependencies {
			implementation(libs.kotlin.crypto.openssl)
		}
		jvmTest.dependencies {
		}
		iosMain.dependencies {
		}
	}
}
