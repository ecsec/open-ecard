description = "Smartcard PCSC Implementation"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":smartcard:sc-base"))
		}

		jvmMain.dependencies {
			implementation(libs.scio)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}

		jvmTest.dependencies {
			implementation(libs.bundles.test.jvm.kotlin)
		}
	}
}
