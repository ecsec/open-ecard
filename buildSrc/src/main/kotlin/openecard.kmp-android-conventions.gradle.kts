plugins {
	id("openecard.kmp-conventions")
	id("com.android.kotlin.multiplatform.library")
}

kotlin {
	androidTarget {
		publishLibraryVariants("release", "debug")
	}
	androidLibrary {
		minSdk = 21
		compileSdk = 34
	}
}
