description = "Smartcard interfaces"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			api(project(":utils:common"))
			api(project(":utils:serialization"))
			api(libs.kotlin.coroutines.core)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}

		jvmTest.dependencies {
			implementation(libs.junit.params)
		}
	}
}
