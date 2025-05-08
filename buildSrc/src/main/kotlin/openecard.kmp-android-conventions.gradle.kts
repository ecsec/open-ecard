plugins {
	id("openecard.kmp-conventions")
	id("com.android.kotlin.multiplatform.library")
}

kotlin {
	androidTarget {
		publishAllLibraryVariants()
	}
	androidLibrary {
		minSdk = 21
		compileSdk = 34
	}
}
