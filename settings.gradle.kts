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
include("gui:about")
include("ifd:ifd-common")
include("ifd:ifd-core")
include("ifd:scio-backend:pcsc")
include("ifd:scio-backend:mobile-nfc")
include("ifd:scio-backend:android-nfc")
include("ifd:scio-backend:ios-nfc")
include("ifd:ifd-protocols:pace")
include("sal:sal-common")
include("sal:tiny-sal")
include("management")
include("bindings:http")
include("addon")
include("addons:chipgateway")
include("addons:genericcryptography")
include("addons:pin-management")
include("addons:status")
include("addons:tr03112")
include("clients:richclient")
include("clients:mobile-lib")
include("clients:ios-common")
include("clients:ios-lib")
include("clients:android-common")
include("clients:android-lib")
include("clients:ios-framework")


dependencyResolutionManagement {

	repositories {
		mavenLocal()
		google()
		mavenCentral()

		maven {
			url = uri("https://mvn.ecsec.de/repository/openecard-public")
		}

		maven {
			url = uri("https://javacard.pro/maven/")
		}
	}

	versionCatalogs {

		create("libs") {
			from(files("libs.versions.toml"))

//			library("nimbus-oidc", "com.nimbusds:oauth2-oidc-sdk:9.5.3")
//
//			plugin("xjc-codegen", "com.github.bjornvester.xjc").version("1.8.1")
//			plugin("jsonschema2pojo", "org.jsonschema2pojo").version("1.2.1")
		}
	}
}
