description = "mobile-lib"

plugins {
	id("openecard.lib-conventions")
}

sourceSets {
	create("ios") {
		java {
			srcDir("src/main/java")
		}
		compileClasspath += main.get().compileClasspath
		runtimeClasspath += main.get().runtimeClasspath
		annotationProcessorPath += main.get().annotationProcessorPath
	}
}

//configurations {
//	this.named("iosImplementation") {
//		extendsFrom(implementation.get())
//	}
//	this.named("iosAnnotationProcessor") {
//		extendsFrom(annotationProcessor.get())
//	}
//}

// TODO: do this in a second compile job so we generate two jars
tasks.named("compileIosJava", JavaCompile::class) {
	this.options.compilerArgs.let {
		it.add("-processor")
		it.add("org.openecard.robovm.processor.RobofaceProcessor")
		it.add("-Aroboface.headername=open-ecard-mobile-lib.h")
		it.add("-Aroboface.inheritance.blacklist=java.io.Serializable")
	}
}

tasks.create("iosJar", Jar::class) {
	dependsOn("iosClasses")
	tasks.named("build") {
		dependsOn("iosJar")
	}
	from(sourceSets.getByName("ios").output)
	archiveClassifier.set("iOS")
}


dependencies {
	implementation(libs.robovm.rt)
	implementation(libs.robovm.cocoa)
	implementation(libs.roboface.annots)
	implementation(libs.roboface.marshal)

//	api(libs.jaxb.api)
//	api(libs.jaxb.ws.api)
//	api(libs.slf4j.api)
	api(project(":management"))
	api(project(":sal:tiny-sal"))
	api(project(":addon"))
	api(project(":addons:tr03112"))
	api(project(":addons:pin-management"))
	api(project(":addons:status"))
	api(project(":addons:genericcryptography"))
	api(project(":ifd:ifd-protocols:pace"))
//	api(project(":wsdef:wsdef-client"))
//	implementation(project(":i18n"))
//	api(libs.bc.prov)
//	api(libs.bc.tls)
//	api(libs.httpcore)

	annotationProcessor(libs.roboface.processor)

	testImplementation(libs.bundles.test.basics)
	testImplementation(project(":ifd:scio-backend:mobile-nfc"))
	testImplementation(project(":wsdef:jaxb-marshaller"))
}
