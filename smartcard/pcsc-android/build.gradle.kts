description = "Smartcard PCSC Implementation for android"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-android-lib-conventions")
}

kotlin {

	sourceSets {
		androidMain.dependencies {
			implementation(libs.kotlin.logging.android)
			api(project(":smartcard:sc-base"))
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}

		androidHostTest.dependencies { }
		androidDeviceTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(libs.androidx.test.core.ktx)
			implementation(libs.androidx.test.junit)
			implementation(libs.androidx.test.runner)
		}
	}
	configurations.all {
		resolutionStrategy.eachDependency {
			if (requested.group == "org.slf4j") {
				useVersion("1.7.36")
				because("i said so")
			}
		}
	}
}
