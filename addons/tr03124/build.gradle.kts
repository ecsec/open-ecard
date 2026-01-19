import java.net.URI

description = "TR03124 implementation"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-spm-ios-conventions")
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":smartcard:pace"))
			api(project(":sal:smartcard-sal"))
			api(project(":cif:bundled-cifs"))

			implementation(libs.kotlin.serialization.core)
			implementation(libs.kotlin.serialization.xml)
			implementation(libs.ktor.serde.json)

			implementation(libs.ktor.client.core)
			implementation(libs.ktor.client.nego)
			implementation(libs.ktor.serde.xml)
			api(libs.ktor.client.logging)
		}

		jvmMain.dependencies {
			implementation(libs.ktor.client.okhttp)
			implementation(libs.bc.tls)
			implementation(libs.bc.prov)
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
			implementation(libs.ktor.client.cio)
			implementation(libs.ktor.serde.json)
		}

		jvmTest.dependencies {
			implementation(libs.junit.params)
			implementation(libs.logback.classic)
			implementation(project(":smartcard:pcsc-native"))
		}

		iosMain.dependencies {
			implementation(project(":utils:openssl-interop"))
			implementation(libs.ktor.client.darwin)
			implementation(libs.ktor.client.cio)
		}

		listOf(
			iosArm64(),
			iosSimulatorArm64(),
		).forEach {
			it.compilations {
				val main by getting {
					cinterops.create("SwiftNio")
				}
			}
		}
	}
}

val iosPlatformVersion: String by project
swiftPackageConfig {
	val path = "${project.layout.buildDirectory.dir("SPM").get().asFile.path}"
	create("SwiftNio") {
		minIos = iosPlatformVersion
		spmWorkingPath = path
		dependency {
			remotePackageVersion(
				url = URI("https://github.com/swift-server/async-http-client.git"),
				products = {
					add("AsyncHTTPClient")
				},
				version = libs.versions.swiftNio.get(),
				packageName = "async-http-client",
			)
		}
		dependency {
			remotePackageVersion(
				url = URI("https://github.com/apple/swift-nio-extras"),
				version = libs.versions.swiftNio.get(),
				products = {
					add("NIOHTTPTypesHTTP1")
					add("NIOHTTPTypes")
					add("NIOExtras")
				},
			)
		}
	}
}
