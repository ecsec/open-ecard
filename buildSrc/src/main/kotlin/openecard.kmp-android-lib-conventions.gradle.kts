
plugins {
	id("openecard.kmp-conventions")
	id("com.android.kotlin.multiplatform.library")
}

kotlin {
	androidLibrary {
		namespace = "org.openecard"

		minSdk = 21
		compileSdk = 34

		withHostTestBuilder { }
		withDeviceTestBuilder {
// 			sourceSetTreeName = "androidDeviceTest"
		}.configure {
			instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
			packaging {
				resources.excludes.add("META-INF/*")
			}
		}
	}
}
