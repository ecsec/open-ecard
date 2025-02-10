import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("openecard.lib-conventions")
	alias(libs.plugins.shadow)
}

dependencies {
	implementation(project(":clients:ios-lib"))
}

tasks.named("shadowJar", ShadowJar::class) {
	relocate("org.apache.http", "oec.apache.http")
}
