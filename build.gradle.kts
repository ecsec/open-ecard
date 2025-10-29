plugins {
	alias(libs.plugins.versions)
	id("openecard.release-info-conventions")
}

tasks.buildReleaseInfo {
	verificationKeyFile = rootDir.resolve("releases/src/commonMain/moko-resources/files/release-verifier.pem")
	versionStatusFile = projectDir.resolve("version-status.json")
	tagPrefix = "richclient-v"

	// artifactHashesFile.set(projectDir.resolve("artifacts.sha256sum"))
	// currentVersionIsLatest.set(true)
}
