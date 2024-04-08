plugins {
	`java-library`
	id("openecard.publish-conventions")
}

val javaToolchain: String by project
//java.sourceCompatibility = JavaVersion.VERSION_1_8
java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaToolchain))
	}
	withSourcesJar()
	withJavadocJar()
}


tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}
