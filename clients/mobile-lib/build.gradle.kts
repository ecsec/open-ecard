@file:OptIn(ExperimentalPathApi::class)

import org.jetbrains.kotlin.incremental.createDirectory
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

description = "mobile-lib"

plugins {
	id("openecard.lib-multiplatform-conventions")
}

val roboHeaderTargetDirStr = "generated/sources/headers/roboface/roboMain"

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlin.logging)
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
			}
		}
		val jvmMain by getting {
			dependencies {
				compileOnly(libs.robovm.rt)
				compileOnly(libs.robovm.cocoa)
				compileOnly(libs.roboface.annots)
				implementation(libs.roboface.marshal)

				// must be compileOnly instead of annotationProcessor, otherwise it is not accessible in the additional compilation
				compileOnly(libs.roboface.processor)

				api(project(":management"))
				api(project(":sal:tiny-sal"))
				api(project(":addons:tr03112"))
				api(project(":addons:pin-management"))
				api(project(":addons:status"))
				api(project(":addons:genericcryptography"))
				api(project(":ifd:ifd-protocols:pace"))

				implementation(libs.annotations)
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(project(":ifd:scio-backend:mobile-nfc"))
				implementation(project(":wsdef:jaxb-marshaller"))
			}
		}
	}

	jvm {
		compilations {
			val main by getting
			val roboMain by compilations.creating {

				compileJavaTaskProvider?.configure {

					options.annotationProcessorPath = main.compileDependencyFiles
					options.compilerArgs.let {
						it.add("-processor")
						it.add("org.openecard.robovm.processor.RobofaceProcessor")
						it.add("-Aroboface.headername=open-ecard-mobile-lib.h")
						it.add("-Aroboface.inheritance.blacklist=java.io.Serializable")
					}

					val roboHeaderTargetDir = layout.buildDirectory.dir(roboHeaderTargetDirStr).get()
					outputs.dir(roboHeaderTargetDir)

					doLast {
						val genHeaders = layout.buildDirectory.dir("classes/java/roboMain/roboheaders").get()
						roboHeaderTargetDir.asFile.toPath().deleteRecursively()
						roboHeaderTargetDir.asFile.parentFile.createDirectory()
						Files.move(genHeaders.asFile.toPath(), roboHeaderTargetDir.asFile.toPath())
					}
				}

				defaultSourceSet {
					dependencies {
						implementation(main.compileDependencyFiles + main.output.classesDirs)
					}
				}
			}
		}
	}
}

val ios by configurations.creating {
	isCanBeConsumed = true
	isCanBeResolved = false
}

val iosHeaders by configurations.creating {
	isCanBeResolved = true
}

val shareHeader =
	tasks.register("shareHeader") {
		dependsOn("roboMainClasses")

		outputs.file(
			layout.buildDirectory.dir(roboHeaderTargetDirStr),
		)
	}

val iosJar =
	tasks.register("iosJar", Jar::class) {
		group = "build"
		dependsOn("roboMainClasses")
		from(sourceSets.getByName("roboMain").output)
		archiveClassifier.set("iOS")
	}
tasks.named("build") {
	dependsOn("iosJar")
	dependsOn("shareHeader")
}

artifacts {
	add(ios.name, iosJar)
	add(iosHeaders.name, shareHeader)
}
