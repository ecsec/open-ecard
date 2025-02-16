description = "Create a shadowed build of httpcore for usage in openecard and mitigate problems with robovm."

plugins {
	id("openecard.lib-conventions")
	id("robovm")
}

val iosHeaders: Configuration by configurations.creating {
	isCanBeResolved = true
}

dependencies {
	iosHeaders(project(path=":clients:mobile-lib", configuration=iosHeaders.name))
	iosHeaders(project(path=":clients:ios-common", configuration=iosHeaders.name))
	iosHeaders(project(path=":clients:ios-lib", configuration=iosHeaders.name))

	api(libs.slf4j.jdk14)
	api(project(path=":clients:ios-lib-shade", configuration="shadow"))
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

tasks.named("jar") { dependsOn("copyHeaders") }

robovm {
	isEnableBitcode = false
}

tasks.register("copyHeaders", Copy::class){
	dependsOn("compileJava")
	val sharedFiles = iosHeaders
	inputs.files(sharedFiles)

	from(sharedFiles)
	into(layout.buildDirectory.dir("classes/java/main/roboheaders/"))
}

tasks.named("robovmInstall") { dependsOn("copyHeaders") }
tasks.create("buildIosFramework"){
	group = "Distribution"
	description = "Alias for robovmInstall creating xcFramework for iOS"
	dependsOn("robovmInstall")

}
