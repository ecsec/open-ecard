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
}
