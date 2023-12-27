pluginManagement {
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "robovm") {
				useModule("com.mobidevelop.robovm:robovm-gradle-plugin:${requested.version}")
			}
		}
	}
	repositories {
		gradlePluginPortal()
        mavenCentral()
    }
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


dependencyResolutionManagement {

	repositories {
		mavenLocal()
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
			// logging
			library("slf4j-api", "org.slf4j", "slf4j-api").version("2.0.5")
			library("slf4j-jdk14", "org.slf4j", "slf4j-jdk14").version("2.0.5")
			library("logback-classic", "ch.qos.logback", "logback-classic").version("1.4.5")

			// utilities
			library("annotations", "com.google.code.findbugs", "annotations").version("3.0.1u2")

			// testing
			library("testng", "org.testng", "testng").version("7.7.0")
			library("powermock-testng", "org.powermock", "powermock-module-testng").version("2.0.9")
			library("powermock-mockito", "org.powermock", "powermock-api-mockito2").version("2.0.9")
			bundle("test-basics", listOf("testng", "powermock-testng", "powermock-mockito"))
			library("awaitility", "org.awaitility", "awaitility").version("4.2.0")

			// jaxb
			library("jaxb-api", "jakarta.xml.bind", "jakarta.xml.bind-api").version("4.0.0")
			library("jaxb-ws-api", "jakarta.xml.ws", "jakarta.xml.ws-api").version("4.0.0")
			library("jaxb-impl", "org.glassfish.jaxb", "jaxb-runtime").version("4.0.2")
			plugin("cxf", "io.mateo.cxf-codegen").version("2.2.0")
			library("jackson-jaxb", "com.fasterxml.jackson.module", "jackson-module-jakarta-xmlbind-annotations").version("2.14.0")

//			library("bc-prov", "org.bouncycastle:bcprov-jdk15on:1.62")
//			library("bc-pkix", "org.bouncycastle:bcpkix-jdk15on:1.62")
//			library("bc-tls", "org.bouncycastle:bctls-jdk15on:1.62")
			library("bc-prov", "org.openecard:bcprov-jdk15on:1.62")
			library("bc-pkix", "org.openecard:bcpkix-jdk15on:1.62")
			library("bc-tls", "org.openecard:bctls-jdk15on:1.62")

			library("httpcore", "org.apache.httpcomponents", "httpcore").version("4.4.15")
			// https://github.com/akuhtz/proxy-vole
			library("proxyvole", "org.bidib.com.github.markusbernhardt", "proxy-vole").version("1.1.2")
//			library("nashorn", "org.javadelight", "delight-nashorn-sandbox").version("0.2.5")

			library("pdfbox", "org.apache.pdfbox", "pdfbox").version("2.0.27")

			library("scio", "com.github.martinpaljak", "apdu4j-jnasmartcardio").version("0.2.7+220204")

			library("android", "com.google.android", "android").version("api-33")

//			version("robovm", "2.3.19-SNAPSHOT")
			version("robovm", "2.3.20")
			library("robovm-gradle", "com.mobidevelop.robovm", "robovm-gradle-plugin").versionRef("robovm")
			library("robovm-rt", "com.mobidevelop.robovm", "robovm-rt").versionRef("robovm")
			library("robovm-cocoa", "com.mobidevelop.robovm", "robovm-cocoatouch").versionRef("robovm")
			library("roboface-annots", "org.openecard.tools", "roboface-annotation").version("1.4.0")
			library("roboface-marshal", "org.openecard.tools", "roboface-marshaller").version("1.4.0")
			library("roboface-processor", "org.openecard.tools", "roboface-processor").version("1.4.0")

			library("jose4j", "org.bitbucket.b_c", "jose4j").version("0.9.1")

			library("jna-jpms", "net.java.dev.jna", "jna-jpms").version("5.12.1")
			library("jna-jpms-platform", "net.java.dev.jna", "jna-platform-jpms").version("5.12.1")

			version("jfx", "17.0.2")
			plugin("jfx", "org.openjfx.javafxplugin").version("0.1.0")
//			library("jfx-base", "org.openjfx", "javafx-base").version("17.0.2")
//			library("jfx-controls", "org.openjfx", "javafx-controls").version("17.0.2")
//			library("jfx-swing", "org.openjfx", "javafx-swing").version("17.0.2")

			plugin("jpackage", "org.panteleyev.jpackageplugin").version("1.6.0")

//			library("nimbus-oidc", "com.nimbusds:oauth2-oidc-sdk:9.5.3")
//
//			plugin("xjc-codegen", "com.github.bjornvester.xjc").version("1.8.1")
//			plugin("jsonschema2pojo", "org.jsonschema2pojo").version("1.2.1")
		}
	}
}
