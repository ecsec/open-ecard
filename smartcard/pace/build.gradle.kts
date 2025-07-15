description = "PACE software implementation"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	// id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":utils:common"))
			api(project(":smartcard:sc-base"))
			api(project(":smartcard:eac-definitions"))
// 			implementation(libs.kotlin.crypto.core)
			implementation(libs.kotlin.crypto.asn1.core)
			implementation(libs.kotlin.crypto.asn1.modules)
		}

		jvmMain.dependencies {
			implementation(libs.kotlin.crypto.jvm)
			implementation(libs.bc.prov)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}

		jvmTest.dependencies {
			implementation(libs.junit.params)
			implementation(project(":smartcard:pcsc-scio"))
		}
	}
}
