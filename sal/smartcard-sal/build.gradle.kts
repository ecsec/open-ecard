description = "Smartcard SAL"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":sal:sal-base"))
			api(project(":smartcard:sc-base"))
			api(project(":cif:cif-definition"))
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(project(":cif:cif-definition-builder"))
		}

		jvmTest.dependencies {
			implementation(project(":smartcard:pace"))
		}
	}
}
