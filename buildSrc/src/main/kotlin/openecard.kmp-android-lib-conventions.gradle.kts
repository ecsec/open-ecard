plugins {
	id("openecard.kmp-conventions")
	id("com.android.kotlin.multiplatform.library")
}

kotlin {
	androidLibrary {
		namespace = "org.openecard"

		minSdk = 21
		compileSdk = 36

		withHostTestBuilder { }
		withDeviceTestBuilder { }.configure {
			instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
			packaging {
				resources.excludes.add("META-INF/*")
			}
		}
	}
}
