description = "Smartcard PCSC Implementation for ios"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		iosMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":smartcard:sc-base"))
			api(project(":utils:common"))
		}
		iosTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
	listOf(
		iosArm64(),
		iosSimulatorArm64(),
	).forEach {
		it.binaries.framework {
			baseName = "openecard_pcscIos"
			isStatic = true
		}
	}
}
