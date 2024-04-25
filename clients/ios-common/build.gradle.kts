import com.android.build.gradle.internal.tasks.factory.dependsOn

description = "ios-common"

plugins {
	id("openecard.lib-conventions")
}

tasks.named("compileJava", JavaCompile::class) {
	this.options.compilerArgs.let {
		it.add("-processor")
		it.add("org.openecard.robovm.processor.RobofaceProcessor")
		it.add("-Aroboface.headername=open-ecard-ios-common.h")
		it.add("-Aroboface.include.headers=open-ecard-mobile-lib.h")
	}
	dependsOn("shareHeader")
}

val shareHeader = tasks.register("shareHeader"){
	outputs.file(
		layout.buildDirectory.file("classes/java/main/roboheaders/open-ecard-ios-common.h")
	)
}
tasks.named("jar").dependsOn("shareHeader")

val iosHeaders by configurations.creating {
	isCanBeResolved = true
}

artifacts {
	add(iosHeaders.name, shareHeader)
}

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

	annotationProcessor(libs.roboface.processor)

	testImplementation(libs.bundles.test.basics)
}
