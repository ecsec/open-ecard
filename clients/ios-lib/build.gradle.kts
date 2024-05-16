import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "ios-lib"

plugins {
	id("openecard.iosbundle-conventions")
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

tasks.named("compileJava", JavaCompile::class) {
	this.options.compilerArgs.let {
		it.add("-processor")
		it.add("org.openecard.robovm.processor.RobofaceProcessor")
		it.add("-Aroboface.headername=open-ecard.h")
		it.add("-Aroboface.include.headers=open-ecard-ios-common.h")
	}
}

tasks.named("shadowJar", ShadowJar::class) {
	relocate("org.apache.http", "oec.apache.http")
}

val shareHeader = tasks.register("shareHeader"){
	outputs.file(
		layout.buildDirectory.file("classes/java/main/roboheaders/open-ecard.h")
	)
}

val iosHeaders: Configuration by configurations.creating {
	isCanBeResolved = true
}

artifacts {
	add(iosHeaders.name, shareHeader)
}

tasks.named("jar").dependsOn("shareHeader")
tasks.named("javadoc").dependsOn("shareHeader")
tasks.named("shadowJar").dependsOn("shareHeader")

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

	annotationProcessor(libs.roboface.processor)

	testImplementation(libs.bundles.test.basics)
//	testImplementation(project(":ifd:scio-backend:mobile-nfc"))
}
