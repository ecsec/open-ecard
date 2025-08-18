pluginManagement {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "openecard-app"

include("utils:common")
include("utils:serialization")
include("utils:test")

include("smartcard:sc-base")
include("smartcard:pcsc-scio")
include("smartcard:pcsc-android")
include("smartcard:eac-definitions")
include("smartcard:pace-base")

include("sal:sal-base")
include("sal:smartcard-sal")

include("cif:cif-dsl-api")
include("cif:cif-definition-builder")
include("cif:cif-definition")
include("cif:bundled-cifs")

include("releases")
include("build-info")

include("wsdef:wsdef-common")
include("wsdef:wsdef-client")
include("wsdef:jaxb-marshaller")
include("i18n")
include("cifs")
include("common")
include("crypto:crypto-common")
include("crypto:tls")
include("gui:graphics")
include("gui:swing")
include("ifd:ifd-common")
include("ifd:ifd-core")
include("ifd:scio-backend:pcsc")
// include("ifd:scio-backend:mobile-nfc")
// include("ifd:scio-backend:android-nfc")
// include("ifd:scio-backend:ios-nfc")
include("ifd:ifd-protocols:pace")
include("sal:sal-common")
include("sal:tiny-sal")
include("management")
include("bindings:http")
include("bindings:ktor")
include("addon")
include("addons:chipgateway")
include("addons:genericcryptography")
include("addons:pin-management")
include("addons:status")
include("addons:tr03112")
// include("addons:cardlink")

include("addons:tr03124")

include("clients:richclient")
// include("clients:mobile-lib")
// include("clients:ios-common")
// include("clients:ios-lib")
// include("clients:android-common")
// include("clients:android-lib")
// include("clients:desktop-lib")

dependencyResolutionManagement {

	repositories {
		// mavenLocal()
		google()
		mavenCentral()

		maven {
			url = uri("https://mvn.ecsec.de/repository/openecard-public")
		}

		maven {
			url = uri("https://mvn.javacard.pro/maven/")
		}

		// repo with systray startup fix
		maven {
			url = uri("https://maven.martmists.com/snapshots")
		}
	}

	versionCatalogs {

		create("libs") {
			from(files("libs.versions.toml"))

// 			library("nimbus-oidc", "com.nimbusds:oauth2-oidc-sdk:9.5.3")
//
// 			plugin("xjc-codegen", "com.github.bjornvester.xjc").version("1.8.1")
// 			plugin("jsonschema2pojo", "org.jsonschema2pojo").version("1.2.1")
		}
	}
}
