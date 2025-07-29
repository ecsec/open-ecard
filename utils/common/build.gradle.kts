description = "Common utilities"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}

		jvmTest.dependencies {
		}
	}
}
