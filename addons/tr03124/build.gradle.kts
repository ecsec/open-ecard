description = "TR03124 implementation"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	// id("openecard.kmp-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			// api(project(":utils:common"))
			// api(project(":smartcard:sc-base"))
			implementation(project(":smartcard:pace"))
			api(project(":sal:smartcard-sal"))
			implementation(project(":cif:bundled-cifs"))

			implementation(libs.kotlin.serialization.core)
			implementation(libs.kotlin.serialization.xml)

			implementation(libs.ktor.client.core)
			implementation(libs.ktor.client.nego)
			implementation(libs.ktor.serde.xml)
		}

		jvmMain.dependencies {
			implementation(libs.ktor.client.okhttp)
			implementation(libs.bc.tls)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(libs.ktor.client.cio)
		}

		jvmTest.dependencies {
			implementation(libs.junit.params)
			implementation(project(":smartcard:pcsc-scio"))
		}
	}
}
