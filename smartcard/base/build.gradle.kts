description = "Smartcard interfaces"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain {
			dependencies {
				implementation(libs.kotlin.logging)
			}
		}
		commonTest {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
			}
		}
	}
}
