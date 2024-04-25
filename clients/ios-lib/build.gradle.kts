import com.android.build.gradle.internal.tasks.factory.dependsOn

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

val iosHeaders : Configuration by configurations.creating {
	isCanBeResolved = true
}

tasks.register("copyHeaders", Copy::class){
	dependsOn("compileJava")
	val sharedFiles = iosHeaders
	inputs.files(sharedFiles)

	from(sharedFiles)
	into(layout.buildDirectory.dir("classes/java/main/roboheaders/"))
}

tasks.named("robovmInstall").dependsOn("copyHeaders")
tasks.named("javadoc").dependsOn("copyHeaders")
tasks.named("jar").dependsOn("copyHeaders")

dependencies {

	iosHeaders(project(path=":clients:mobile-lib", configuration=iosHeaders.name))
	iosHeaders(project(path=":clients:ios-common", configuration=iosHeaders.name))

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

	annotationProcessor(libs.roboface.processor)

	testImplementation(libs.bundles.test.basics)
//	testImplementation(project(":ifd:scio-backend:mobile-nfc"))
}
