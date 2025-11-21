pluginManagement {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "openecard-app"

include("utils:common")
include("utils:serialization")

include("smartcard:sc-base")
include("smartcard:pcsc-native")
include("smartcard:pcsc-android")
include("smartcard:pcsc-ios")
include("smartcard:eac-definitions")
include("smartcard:pace")

include("sal:sal-base")
include("sal:smartcard-sal")

include("cif:cif-dsl-api")
include("cif:cif-definition-builder")
include("cif:cif-definition")
include("cif:bundled-cifs")

include("releases")
include("build-info")

include("bindings:ktor")

include("addons:tr03124")

include("i18n")
include("clients:richclient-res")
include("clients:richclient")

dependencyResolutionManagement {

	repositories {
		// mavenLocal()
		google()
		mavenCentral()

		maven {
			name = "Central Portal Snapshots"
			url = uri("https://central.sonatype.com/repository/maven-snapshots/")
		}

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
		}
	}
}
