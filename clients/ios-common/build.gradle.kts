@file:OptIn(ExperimentalPathApi::class)

import org.jetbrains.kotlin.incremental.createDirectory
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

description = "ios-common"

plugins {
	id("openecard.lib-conventions")
}

val roboHeaderTargetDirStr = "generated/sources/headers/roboface/main"


dependencies {
	implementation(libs.robovm.rt)
	implementation(libs.robovm.cocoa)
	compileOnly(libs.roboface.annots)
	implementation(libs.roboface.marshal)

	api(project(":clients:mobile-lib", "ios"))
//	api(project(":management"))
//	api(project(":sal:tiny-sal"))
//	api(project(":addon"))
//	api(project(":addons:tr03112"))
//	api(project(":addons:pin-management"))
//	api(project(":addons:status"))
//	api(project(":addons:genericcryptography"))
//	api(project(":ifd:ifd-protocols:pace"))
	api(project(":ifd:scio-backend:ios-nfc"))

	implementation(libs.xerces.imp)

	annotationProcessor(libs.roboface.processor)
}


tasks.named("compileJava", JavaCompile::class) {
	this.options.compilerArgs.let {
		it.add("-processor")
		it.add("org.openecard.robovm.processor.RobofaceProcessor")
		it.add("-Aroboface.headername=open-ecard-ios-common.h")
		it.add("-Aroboface.include.headers=open-ecard-mobile-lib.h")
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
