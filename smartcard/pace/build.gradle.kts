description = "PACE software implementation"

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
			api(project(":utils:common"))
			api(project(":smartcard:sc-base"))
			api(project(":smartcard:eac-definitions"))
			implementation(libs.kotlin.crypto.core)
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
			implementation(libs.bundles.test.jvm.kotlin)
			implementation(project(":smartcard:pcsc-scio"))
		}

		nativeMain.dependencies {
			implementation(libs.kotlin.crypto.random)
			implementation(libs.kotlin.crypto.openssl)
		}
	}
}
