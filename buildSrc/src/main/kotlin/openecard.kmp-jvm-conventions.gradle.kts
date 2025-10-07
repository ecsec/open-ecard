import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("openecard.kmp-conventions")
}

val javaTarget: String by project

kotlin {
	jvm {
		compilerOptions.jvmTarget = JvmTarget.fromTarget(javaTarget)
		compilations {
			val test by getting {
// 				tasks.named<Test>("jvmTest") {
// 					useTestNG {
// 						excludeGroups("interactive", "broken")
// 					}
// 				}
// 				// only run interactive tests
// 				tasks.register<Test>("interactiveTest") {
// 					group = "verification"
// 					// Run the tests with the classpath containing the compile dependencies (including 'main'),
// 					// runtime dependencies, and the outputs of this compilation:
// 					classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs
//
// 					// Run only the tests from this compilation's outputs:
// 					testClassesDirs = output.classesDirs
// 					useTestNG {
// 						includeGroups("interactive")
// 					}
// 				}
			}
		}
	}
}

java {
	val jVersion = JavaVersion.toVersion(javaTarget)
	sourceCompatibility = jVersion
	targetCompatibility = jVersion
}
