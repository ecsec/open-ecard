import com.android.build.gradle.internal.tasks.factory.dependsOn

description = "Create a shadowed build of httpcore for usage in openecard and mitigate problems with robovm."

plugins {
	id("openecard.iosbundle-conventions")
	id("robovm").version(libs.versions.robovm)
}
val iosHeaders: Configuration by configurations.creating {
	isCanBeResolved = true
}

dependencies {
	iosHeaders(project(path=":clients:mobile-lib", configuration=iosHeaders.name))
	iosHeaders(project(path=":clients:ios-common", configuration=iosHeaders.name))
	iosHeaders(project(path=":clients:ios-lib", configuration=iosHeaders.name))

	api(project(path=":clients:ios-lib", configuration="shadow"))
	implementation(libs.robovm.rt)
	implementation(libs.robovm.cocoa)
}

tasks.named("jar").dependsOn("copyHeaders")

robovm {
	isEnableBitcode = true
}

tasks.register("copyHeaders", Copy::class){
	dependsOn("compileJava")
	val sharedFiles = iosHeaders
	inputs.files(sharedFiles)

	from(sharedFiles)
	into(layout.buildDirectory.dir("classes/java/main/roboheaders/"))
}

tasks.named("robovmInstall").dependsOn("copyHeaders")
