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
			// TODO: it seems we need brainpool curves and compressed keys
			// these are still missing in kotlin-crypto, but the author said he wants to make a release in mid June
			// https://github.com/whyoleg/cryptography-kotlin/pull/57#discussion_r2098682916
			// https://github.com/whyoleg/cryptography-kotlin/pull/60
			implementation(libs.kotlin.crypto.core)
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
		}
	}
}
