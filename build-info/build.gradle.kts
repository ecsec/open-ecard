import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec


description = "Build Info"

plugins {
	id("openecard.kmp-lib-conventions")
	id("openecard.kmp-jvm-conventions")
	id("openecard.kmp-ios-conventions")
}

val generatedKotlinSourcesDir = layout.buildDirectory.dir("generated/commonMain/kotlin")

kotlin {
	sourceSets {
		commonMain {
			kotlin.srcDir(generatedKotlinSourcesDir)
			dependencies {
				api(libs.semver)
			}
		}

		commonTest.dependencies {
			implementation(libs.bundles.test.basics.kotlin)
		}
	}
}

fun generateVersionInfo() {
	// construct a BuildInfo file with all the data we need
	val buildInfoFile =
		FileSpec
			.builder("org.openecard.build", "BuildInfo")
			.addImport("io.github.z4kn4fein.semver", "toVersion")
			.addType(
				TypeSpec
					.objectBuilder("BuildInfo")
					.addProperty(
						PropertySpec
							.builder(
								"version",
								ClassName("io.github.z4kn4fein.semver", "Version"),
							).initializer("%S.toVersion()", project.version)
							.build(),
					).build(),
			).build()
	// TODO: add more data if needed

	val outDir = generatedKotlinSourcesDir.get().asFile
	buildInfoFile.writeTo(outDir)
}

// fiddling with tasks is difficult and the generation is simple, so just do it here
generateVersionInfo()
