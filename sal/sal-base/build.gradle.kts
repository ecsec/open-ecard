description = "SAL interfaces"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			// implementation(libs.kotlin.logging)
			api(libs.kotlin.coroutines.core)
			api(project(":smartcard:sc-base"))
		}

		commonTest.dependencies {
// 			implementation(libs.bundles.test.basics.kotlin)
		}

		jvmTest.dependencies {
// 			implementation(libs.junit.params)
		}
	}
}
