description = "Richclient resources"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.moko-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				api(libs.moko.resources)
			}
		}
	}
}

multiplatformResources {
	resourcesPackage.set("org.openecard.richclient.res") // required
	// resourcesClassName.set("Res") // optional, default MR
}
