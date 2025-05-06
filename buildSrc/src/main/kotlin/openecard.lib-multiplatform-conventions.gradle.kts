// Deprecated

plugins {
	id("openecard.kmp-conventions")
	id("openecard.publish-conventions")
	id("openecard.coverage-conventions")
	kotlin("plugin.serialization")
}

kotlin {
	jvm {
		withJava()
		compilations {
			val test by getting {
				tasks.named<Test>("jvmTest") {
					useTestNG {
						excludeGroups("interactive", "broken")
					}
				}
				// only run interactive tests
				tasks.register<Test>("interactiveTest") {
					group = "verification"
					// Run the tests with the classpath containing the compile dependencies (including 'main'),
					// runtime dependencies, and the outputs of this compilation:
					classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs

					// Run only the tests from this compilation's outputs:
					testClassesDirs = output.classesDirs
					useTestNG {
						includeGroups("interactive")
					}
				}
			}
		}
	}
}

tasks.withType<Test> {
	useTestNG()
}

// set encoding for legacy java tasks, remove once all java is migrated to kotlin
tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}
tasks.withType<Javadoc> {
	options.encoding = "UTF-8"
}
