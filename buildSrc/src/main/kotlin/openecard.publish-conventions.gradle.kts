plugins {
	`maven-publish`
}

publishing {
	repositories {
		maven {
			credentials {
				username = System.getenv("MVN_ECSEC_USERNAME") ?: project.findProperty("mvnUsernameEcsec") as String?
				password = System.getenv("MVN_ECSEC_PASSWORD") ?: project.findProperty("mvnPasswordEcsec") as String?
			}
			val releasesRepoUrl = uri("https://mvn.ecsec.de/repository/openecard-release")
			val snapshotsRepoUrl = uri("https://mvn.ecsec.de/repository/openecard-snapshot")
			url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
		}
	}
}
