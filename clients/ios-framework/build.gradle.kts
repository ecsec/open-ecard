description = "Create a shadowed build of httpcore for usage in openecard and mitigate problems with robovm."

plugins {
	id("openecard.lib-conventions")
}

dependencies {
	api(libs.slf4j.jdk14)
	api(project(path = ":clients:ios-lib"))
	implementation(libs.robovm.rt)
	implementation(libs.robovm.cocoa)
	api(project(":management"))
	api(project(":sal:tiny-sal"))
	api(project(":addons:tr03112"))
	api(project(":addons:pin-management"))
	api(project(":addons:status"))
	api(project(":addons:genericcryptography"))
	api(project(":ifd:ifd-protocols:pace"))
}
