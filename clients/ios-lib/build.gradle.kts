description = "ios-lib"

plugins {
	id("openecard.iosbundle-conventions")
	id("robovm").version(libs.versions.robovm)
}

tasks.named("compileJava", JavaCompile::class) {
	this.options.compilerArgs.let {
		it.add("-processor")
		it.add("org.openecard.robovm.processor.RobofaceProcessor")
		it.add("-Aroboface.headername=open-ecard.h")
		it.add("-Aroboface.include.headers=open-ecard-ios-common.h")
	}
}

robovm {
	isEnableBitcode = true
}


dependencies {
	implementation(libs.robovm.rt)
	implementation(libs.robovm.cocoa)
	compileOnly(libs.roboface.annots)
	implementation(libs.roboface.marshal)

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

	annotationProcessor(libs.roboface.processor)

	testImplementation(libs.bundles.test.basics)
//	testImplementation(project(":ifd:scio-backend:mobile-nfc"))
}
