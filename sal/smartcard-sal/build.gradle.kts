description = "Smartcard SAL"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
	id("openecard.kmp-android-lib-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":sal:sal-base"))
			api(project(":smartcard:sc-base"))
			api(project(":cif:cif-definition"))
			implementation(project(":smartcard:eac-definitions"))
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(project(":cif:cif-definition-builder"))
		}

		jvmTest.dependencies {
			implementation(project(":smartcard:pcsc-scio"))
			implementation(project(":smartcard:pace"))
			implementation(project(":cif:bundled-cifs"))
		}
		androidDeviceTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(libs.androidx.test.core.ktx)
			implementation(libs.androidx.test.junit)
			implementation(libs.androidx.test.runner)
			implementation(project(":smartcard:pcsc-android"))
			implementation(project(":smartcard:pace"))
			implementation(project(":cif:bundled-cifs"))
		}

		configurations.filter { it.name.contains("android") }.forEach {
			it.resolutionStrategy.eachDependency {
				if (requested.group == "org.slf4j") {
					useVersion("1.7.36")
					because("newer versions lead to runtime errors on android")
				}
			}
		}
	}
}
