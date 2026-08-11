description = "i18n resources"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
	// id("openecard.kmp-desktop-conventions")
	id("openecard.moko-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			api(libs.moko.resources)
		}
		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}

		jvmMain.dependencies {}

		jvmTest.dependencies {}
	}
}

multiplatformResources {
	resourcesPackage.set("org.openecard.i18n") // required
	resourcesClassName.set("I18N") // optional, default MR
}
