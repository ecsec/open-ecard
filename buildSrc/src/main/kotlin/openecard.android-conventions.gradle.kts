plugins {
    id("com.android.library")
	id("openecard.publish-conventions")
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
}
