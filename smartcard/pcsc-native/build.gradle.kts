description = "Native PCSC Implementation"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-desktop-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":smartcard:sc-base"))
			implementation(libs.pcsc)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}
	}
}
