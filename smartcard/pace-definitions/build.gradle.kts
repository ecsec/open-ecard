description = "PACE Definitions"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":utils:common"))
			api(project(":smartcard:sc-base"))
			implementation(libs.kotlin.crypto.asn1.core)
			implementation(libs.kotlin.crypto.asn1.modules)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}
	}
}
