plugins {
	id("openecard.kmp-conventions")
	id("com.android.kotlin.multiplatform.library")
}

kotlin {
	androidLibrary {
		namespace = "org.openecard"

		minSdk = 21
		compileSdk = 36

		// kover has issue with variants if active
		// https://github.com/Kotlin/kotlinx-kover/issues/772
		// deactivating for now
		// withHostTestBuilder { }
		withDeviceTestBuilder { }.configure {
			instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
			packaging {
				resources.excludes.add("META-INF/*")
			}
		}
	}
}
