description = "i18n resources"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
	id("openecard.moko-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				api(libs.moko.resources)
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
			}
		}
		val jvmMain by getting {
			dependencies {}
		}
		val jvmTest by getting {
			dependencies {}
		}
	}
}

multiplatformResources {
	resourcesPackage.set("org.openecard.i18n") // required
	resourcesClassName.set("I18N") // optional, default MR
}

// kover gets confused because no sources are in this project
kover {
	disable()
}
