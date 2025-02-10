@file:OptIn(ExperimentalPathApi::class)

import org.jetbrains.kotlin.incremental.createDirectory
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

description = "ios-lib"

plugins {
	id("openecard.lib-multiplatform-conventions")
}

val roboHeaderTargetDirStr = "generated/sources/headers/roboface/main"


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

				api(project(":clients:ios-common"))
				api(project(":wsdef:jaxb-marshaller"))
				api(libs.httpcore)
				api(project(":addons:cardlink"))

				implementation(libs.xerces.imp)

				implementation(libs.annotations)
			}
		}
		val jvmTest by getting {
			dependencies {
			}
		}
	}

	jvm() {
		val main by compilations.getting {
			compileJavaTaskProvider?.configure {

				options.annotationProcessorPath = compileDependencyFiles
				options.compilerArgs.let {
					it.add("-processor")
					it.add("org.openecard.robovm.processor.RobofaceProcessor")
					it.add("-Aroboface.headername=open-ecard.h")
					it.add("-Aroboface.include.headers=open-ecard-ios-common.h")
				}

				val roboHeaderTargetDir = layout.buildDirectory.dir(roboHeaderTargetDirStr).get()
				outputs.dir(roboHeaderTargetDir)

				doLast {
					val genHeaders = layout.buildDirectory.dir("classes/java/main/roboheaders").get()
					roboHeaderTargetDir.asFile.toPath().deleteRecursively()
					roboHeaderTargetDir.asFile.parentFile.createDirectory()
					Files.move(genHeaders.asFile.toPath(), roboHeaderTargetDir.asFile.toPath())
				}
			}
		}
	}
}


val iosHeaders by configurations.creating {
	isCanBeResolved = true
}

val shareHeader = tasks.register("shareHeader") {
	dependsOn("classes")

	outputs.file(
		layout.buildDirectory.dir(roboHeaderTargetDirStr)
	)
}

tasks.named("build") {
	dependsOn("shareHeader")
}

artifacts {
	add(iosHeaders.name, shareHeader)
}
