description = "Smartcard PCSC Implementation"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			implementation(libs.kotlin.coroutines.core)
			api(project(":smartcard:base"))
		}

		jvmMain.dependencies {
			implementation(libs.scio)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}
	}

	jvm {
		compilations {
			val test by getting {
				// only run interactive tests
				tasks.register<Test>("pcscTest") {
					group = "verification"
					// Run the tests with the classpath containing the compile dependencies (including 'main'),
					// runtime dependencies, and the outputs of this compilation:
					classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs

					// Run only the tests from this compilation's outputs:
					testClassesDirs = output.classesDirs
					useJUnitPlatform {
						excludeTags.clear()
						includeTags("pcsc")
					}
				}
			}
		}
	}
}
