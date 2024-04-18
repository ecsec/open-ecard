plugins {
    id("com.android.library")
    `maven-publish`
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

android {
    compileSdk = 34
    defaultConfig {
	minSdk = 21
    }

    packaging {
	    resources.excludes.add("cif-repo/repo-config.properties")
    }
 
    publishing {
	singleVariant("release") {
	    // if you don't want sources/javadoc, remove these lines
	    withSourcesJar()
	    withJavadocJar()
	}
    }
}
publishing {
    publications {
	register<MavenPublication>("release") {
	    afterEvaluate {
		from(components["release"])
	    }
	}
    }
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
