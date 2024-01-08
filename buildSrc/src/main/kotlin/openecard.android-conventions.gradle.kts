plugins {
	id("com.android.library")
}

val javaToolchain: String by project
java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaToolchain))
	}
//	withSourcesJar()
//	withJavadocJar()
}

android {
	compileSdk = 34
	defaultConfig {
		minSdk = 21
	}
}


tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}
