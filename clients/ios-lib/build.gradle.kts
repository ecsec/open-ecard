@file:OptIn(ExperimentalPathApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.incremental.createDirectory
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

description = "ios-lib"

plugins {
	id("openecard.iosbundle-conventions")
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

val roboHeaderTargetDirStr = "generated/sources/headers/roboface/main"


dependencies {

	implementation(libs.robovm.rt)
	implementation(libs.robovm.cocoa)
	compileOnly(libs.roboface.annots)
	implementation(libs.roboface.marshal)
	constraints {
		val reason = "Newer versions will break our build because of modularization since v4."
		api("jakarta.xml.bind:jakarta.xml.bind-api") {
			version {
				strictly("3.0.1")
			}
			because(reason)
		}
		api("jakarta.xml.ws:jakarta.xml.ws-api") {
			version {
				strictly("3.0.1")
			}
			because(reason)
		}
		api("org.glassfish.jaxb:jaxb-runtime") {
			version {
				strictly("3.0.2-mobile")
			}
			because(reason)
		}
	}
	api(project(":clients:ios-common"))
//	api(project(":management"))
//	api(project(":sal:tiny-sal"))
//	api(project(":addon"))
//	api(project(":addons:tr03112"))
//	api(project(":addons:pin-management"))
//	api(project(":addons:status"))
//	api(project(":addons:genericcryptography"))
//	api(project(":ifd:ifd-protocols:pace"))
	api(project(":wsdef:jaxb-marshaller"))
	api(libs.httpcore)
	api(project(":addons:cardlink"))

	annotationProcessor(libs.roboface.processor)

//	testImplementation(project(":ifd:scio-backend:mobile-nfc"))
}


tasks.named("compileJava", JavaCompile::class) {
	this.options.compilerArgs.let {
		it.add("-processor")
		it.add("org.openecard.robovm.processor.RobofaceProcessor")
		it.add("-Aroboface.headername=open-ecard.h")
		it.add("-Aroboface.include.headers=open-ecard-ios-common.h")
	}

	outputs.dir(roboHeaderTargetDirStr)

	doLast {
		val genHeaders = layout.buildDirectory.dir("classes/java/main/roboheaders").get()
		val targetDir = layout.buildDirectory.dir(roboHeaderTargetDirStr).get()
		targetDir.asFile.toPath().deleteRecursively()
		targetDir.asFile.parentFile.createDirectory()
		Files.move(genHeaders.asFile.toPath(), targetDir.asFile.toPath())
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


tasks.named("shadowJar", ShadowJar::class) {
	relocate("org.apache.http", "oec.apache.http")
}
