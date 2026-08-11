description = "releases"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.moko-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)

			api(libs.semver)

			api(libs.kotlin.serialization.json)

			api(libs.ktor.client.core)
			implementation(libs.ktor.client.logging)

			implementation(libs.jwtkt.core)
			implementation(libs.jwtkt.ecdsa)
			implementation(libs.kotlinx.datetime)

			implementation(libs.moko.resources)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(libs.moko.resourcesTest)
		}

		jvmMain.dependencies {
			implementation(libs.ktor.client.cio)
		}

		jvmTest.dependencies {
			implementation(libs.wiremock)
		}
	}
}
