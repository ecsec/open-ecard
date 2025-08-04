import org.apache.commons.io.output.ByteArrayOutputStream

description = "Smartcard PCSC Implementation for ios"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-ios-conventions")
// 	alias(libs.plugins.swiftKlib)
// 	kotlin("native.cocoapods")
}

kotlin {
	sourceSets {
		iosMain.dependencies {
			implementation(libs.kotlin.logging)
			api(project(":smartcard:sc-base"))
			api(project(":utils:common"))
		}
		iosTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
	listOf(
		iosArm64(),
		iosSimulatorArm64(),
	).forEach {
		it.binaries.framework {
			baseName = "openecard_pcscIos"
			isStatic = true
		}
	}
}

val findConnectedDeviceId =
	tasks.register<Exec>("findConnectedDeviceId") {

		standardOutput = ByteArrayOutputStream()
		commandLine(
			"/opt/homebrew/bin/ios-deploy",
			"-c",
		)

		doLast {
			project.extensions.extraProperties["iosDeviceId"] =
				standardOutput
					.toString()
					.split("\n")
					.firstOrNull { it.contains("Found") && it.contains("connected through USB") }
					?.split(" ")[2] ?: throw GradleException("No connected device found")
		}
	}

tasks.register<Exec>("connectedIosDeviceTest") {
	group = "verification"
	description = "Runs the xc_nfcTest connected iOS device"

	dependsOn(findConnectedDeviceId)

	workingDir("src/xc_nfcTest")

	doFirst {
		commandLine(
			"xcodebuild",
			"-scheme",
			"xCodeTest",
			"-destination",
			"platform=iOS,id=${project.extensions.extraProperties["iosDeviceId"]}",
			"test",
		)
	}
}
