plugins {
	alias(libs.plugins.versions)
	id("openecard.release-info-conventions")
}

tasks.buildReleaseInfo {
	verificationKeyFile.set(rootDir.resolve("releases/src/commonMain/moko-resources/files/release-verifier.pem"))
	versionStatusFile.set(projectDir.resolve("version-status.json"))

	//artifactHashesFile.set(projectDir.resolve("artifacts.sha256sum"))
	//currentVersionIsLatest.set(true)
}
