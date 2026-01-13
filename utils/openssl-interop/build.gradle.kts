import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

description = "Openssl"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-ios-conventions")
	id("openecard.kmp-desktop-conventions")
	id("openecard.kmp-cinterop-conventions")
}

kotlin {

	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}

		nativeMain.dependencies {
		}
	}

	targets.forEach {
		if (it is KotlinNativeTarget) {
			it.compilations.forEach { comp ->
				comp.cinterops {
					val openssl by creating {
						definitionFile.set(project.file("src/nativeInterop/cinterop/openssl.def"))
					}
				}
			}
		}
	}
}
