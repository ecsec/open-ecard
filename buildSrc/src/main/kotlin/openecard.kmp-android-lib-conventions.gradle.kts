plugins {
	id("openecard.kmp-conventions")
	id("com.android.kotlin.multiplatform.library")
}

fun catalogVersion(ref: String) =
	Integer.parseInt(
		the<VersionCatalogsExtension>()
			.named("libs")
			.findVersion(ref)
			.get()
			.requiredVersion,
	)

kotlin {
	androidLibrary {
		namespace = "org.openecard"

		minSdk = catalogVersion("androidMinSdk")
		compileSdk = catalogVersion("androidCompileSdk")

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
