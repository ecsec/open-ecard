description = "Native PCSC Implementation"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	// id("openecard.kmp-desktop-conventions")
}

kotlin {
	// TODO: once we have all native implementations, use desktop convention script
	linuxX64 { }
	mingwX64 { }
	// macosX64 { }

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
