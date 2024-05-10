import com.android.build.gradle.internal.tasks.factory.dependsOn

description = "mobile-lib"

plugins {
	id("openecard.lib-multiplatform-conventions")
}

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlin.logging)
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics.kotlin)
			}
		}
		val jvmMain by getting {
			dependencies {
				compileOnly(libs.robovm.rt)
				compileOnly(libs.robovm.cocoa)
				compileOnly(libs.roboface.annots)
				implementation(libs.roboface.marshal)

				api(project(":management"))
				api(project(":sal:tiny-sal"))
				api(project(":addons:tr03112"))
				api(project(":addons:pin-management"))
				api(project(":addons:status"))
				api(project(":addons:genericcryptography"))
				api(project(":ifd:ifd-protocols:pace"))
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(libs.bundles.test.basics)
				implementation(project(":ifd:scio-backend:mobile-nfc"))
				implementation(project(":wsdef:jaxb-marshaller"))
			}
		}
	}
}

sourceSets {
	create("ios") {
		java {
			srcDir("src/jvmMain/java")
		}
		compileClasspath += main.get().compileClasspath
//		runtimeClasspath += main.get().runtimeClasspath
		annotationProcessorPath += main.get().annotationProcessorPath
	}
}

val ios by configurations.creating {
	isCanBeConsumed = true
	isCanBeResolved = false
}

val iosHeaders by configurations.creating {
	isCanBeResolved = true
}

val compileIosJava = tasks.named("compileIosJava", JavaCompile::class) {
	this.options.compilerArgs.let {
		it.add("-processor")
		it.add("org.openecard.robovm.processor.RobofaceProcessor")
		it.add("-Aroboface.headername=open-ecard-mobile-lib.h")
		it.add("-Aroboface.inheritance.blacklist=java.io.Serializable")
	}
}
val shareHeader = tasks.register("shareHeader "){
	dependsOn("compileIosJava")
	outputs.file(
		layout.buildDirectory.file("classes/java/ios/roboheaders/open-ecard-mobile-lib.h")
	)
}


val iosJar = tasks.create("iosJar", Jar::class) {
	group = "build"
	dependsOn("iosClasses")
	tasks.named("build") {
		dependsOn("iosJar")
	}
	from(sourceSets.getByName("ios").output)
	archiveClassifier.set("iOS")
}

artifacts {
	add(ios.name, iosJar)
	add(iosHeaders.name, shareHeader)
}

dependencies {
	annotationProcessor(libs.roboface.processor)
}
