import com.sun.jna.Platform
import org.gradle.util.internal.VersionNumber
import org.panteleyev.jpackage.ImageType
import org.panteleyev.jpackage.JPackageTask
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.util.regex.Pattern
import java.util.zip.Deflater
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

description = "richclient"

plugins {
	id("openecard.app-conventions")
	alias(libs.plugins.jfx)
	alias(libs.plugins.jpackage)
}

javafx {
	version = libs.versions.jfx.get()
	modules = listOf("javafx.controls")
}

application {
	mainClass = "org.openecard.richclient.RichClient"
}

dependencies {

	implementation(libs.apache.batik)
	implementation(libs.kotlin.logging)
	implementation(libs.logback.classic)
	implementation(project(":crypto:tls"))

	implementation(project(":ifd:ifd-core"))
	implementation(project(":ifd:scio-backend:pcsc"))
	implementation(project(":ifd:ifd-protocols:pace"))
	implementation(project(":sal:tiny-sal"))
	implementation(project(":management"))
	implementation(project(":bindings:http"))
	implementation(project(":gui:swing"))
	implementation(project(":gui:graphics"))

	// addons
	implementation(project(":addons:chipgateway"))
	implementation(project(":addons:genericcryptography"))
	implementation(project(":addons:pin-management"))
	implementation(project(":addons:status"))
	implementation(project(":addons:tr03112"))
	implementation(project(":i18n"))

	// basic runtime deps
	implementation(project(":cifs"))
	implementation(project(":wsdef:jaxb-marshaller"))

	implementation(project(":releases"))
	implementation(project(":build-info"))

	implementation(libs.jose4j)
	implementation(libs.jna.jpms)
	implementation(libs.jna.jpms.platform)
	implementation(libs.systray)

	testImplementation(libs.bundles.test.basics.kotlin)
}

val setAppName = "Open-eCard-App"
val setAppVendor = "ecsec GmbH"
val setAppLicenseFile: String = projectDir.resolve("../../LICENSE.GPL").path
val setAppAboutUrl = "https://openecard.org/"
val setAppVersion =
	VersionNumber.parse(project.version.toString()).let {
		"${it.major}.${it.minor}.${it.micro}"
	}
val macSigningId: String? = System.getenv("MAC_SIGNING_ID")

tasks.register<Copy>("copyDependencies") {
	from(configurations.runtimeClasspath).into(layout.buildDirectory.dir("jars"))
}
tasks.register<Copy>("copyJar") {
	from(tasks.jar).into(layout.buildDirectory.dir("jars"))
}

fun JPackageTask.applyDefaults() {
	input =
		layout.buildDirectory
			.dir("jars")
			.get()
			.toString()
	mainJar =
		tasks.jar
			.get()
			.archiveFileName
			.get()
	mainClass = application.mainClass.get()

	destination =
		layout.buildDirectory
			.dir("dist")
			.get()
			.toString()
	javaOptions =
		listOf(
			"-XX:-UsePerfData",
			"-XX:-Inline",
			"-Xms16m",
			"-Xmx64m",
			"-XX:+UseG1GC",
			"-XX:MinHeapFreeRatio=1",
			"-XX:MaxHeapFreeRatio=5",
			"-XX:G1ReservePercent=5",
			"-Djavax.xml.stream.isSupportingExternalEntities=false",
			"-Djavax.xml.stream.supportDTD=false",
		)
	appName = setAppName
	appVersion = setAppVersion
	vendor = setAppVendor
	licenseFile = setAppLicenseFile
	aboutUrl = setAppAboutUrl
	copyright = "Copyright (C) ${LocalDate.now().year} ecsec GmbH"
	appDescription = "Client side implementation of the eCard-API-Framework (BSI TR-03112)"

	System.getenv("RUNTIME_JDK_PATH")?.let {
		jLinkOptions.add("--modulePath")
		jLinkOptions.add(it)
	}
}

// configs for the packages

fun JPackageTask.linuxConfigs() {
// 	resourceDir = layout.projectDirectory.dir("src/main/package/linux").toString()
	icon = layout.projectDirectory.dir("src/main/package/linux/Open-eCard-App.png").toString()
	linuxDebMaintainer = "tobias.wich@ecsec.de"
	linuxPackageName = "open-ecard-app"
	linuxAppCategory = "utils"
// 	linuxRpmLicenseType = "GPLv3+"
	linuxMenuGroup = "Network"
// 	linuxPackageDeps = false
}

fun JPackageTask.windowsConfigs() {
	resourceDir = layout.projectDirectory.dir("src/main/package/win").toString()

	icon = layout.projectDirectory.dir("src/main/package/win/Open-eCard-App.ico").toString()

	winDirChooser = true
	winMenuGroup = "misc"
	winUpgradeUuid = "B11CB66-71B5-42C1-8076-15F1FEDCC22A"
	winShortcut = true
	winShortcutPrompt = true
}

fun JPackageTask.macConfigs() {
	resourceDir = layout.projectDirectory.dir("src/main/package/mac").toString()

	icon = layout.projectDirectory.dir("src/main/package/mac/Open-eCard-App.icns").toString()
	macPackageName = "Open-eCard-App"
	macPackageIdentifier = "org.openecard.versioncheck.MainLoader"

	if (macSigningId != null) {
		macSign = true
		macSigningKeyUserName = macSigningId
		macSigningKeychain = System.getenv("MAC_SIGNING_KEYCHAIN")
			?: throw IllegalStateException("Please provide a signing keychain via the env variable 'MAC_SIGNING_KEYCHAIN'.")
	} else {
		macSign = false
		logger.warn(
			"Mac signing is disabled, please provide a signing id (Apple TeamID) via the 'MAC_SIGNING_ID' env variable if this is needed.",
		)
	}
}

// linux packaging

tasks.register("packageDeb", JPackageTask::class) {
	group = "Distribution"
	description = "Creates a DEB package for installation."

	onlyIf("OS is not Linux") {
		Platform.isLinux()
	}
	dependsOn("build", "copyDependencies", "copyJar")

	applyDefaults()
	linuxConfigs()

	type = ImageType.DEB
}

tasks.register("packageRpm", JPackageTask::class) {
	group = "Distribution"
	description = "Creates a RPM package for installation."

	onlyIf("OS is not Linux") {
		Platform.isLinux()
	}
	dependsOn("build", "copyDependencies", "copyJar")

	applyDefaults()
	linuxConfigs()

	type = ImageType.RPM
}

tasks.register("packageLinux") {
	group = "Distribution"
	description = "Creates DEB and RPM packages for linux systems."

	dependsOn(
		"packageDeb",
		"packageRpm",
	)
}

// mac packaging

abstract class MacSignLibrariesTask
	@Inject
	constructor(
		private val execProvider: ExecOperations,
	) : DefaultTask() {
		@get:Input
		abstract val signingId: Property<String>

		@get:InputFiles
		abstract val jarFiles: ConfigurableFileTree

		@get:Input
		abstract var filesToSign: List<String>

		@get:Input
		var compressionLevel: Int = Deflater.BEST_COMPRESSION

		@get:OutputDirectory
		abstract var jarFilesSignedOutputDir: Path

		@TaskAction
		fun signFiles() {
			val buildDirectory =
				Path.of(
					project.layout.buildDirectory.asFile
						.get()
						.path,
					"mac-sign",
				)
			buildDirectory.toFile().deleteRecursively()
			Files.createDirectory(buildDirectory)

			// The JNA libraries are not signed, so we're signing them as soon as all JARS are copied to the build folder
			// If we need to sign in future additional libraries, this can also be done here
			for (jarFile in jarFiles) {
				val zipFile = ZipFile(jarFile)

				val zipFileSignedPath = buildDirectory.resolve(jarFile.name)
				val zipFileSigned = ZipOutputStream(zipFileSignedPath.toFile().outputStream())
				zipFile.comment?.let { zipFileSigned.setComment(it) }
				zipFileSigned.setLevel(compressionLevel)

				zipFile.entries().iterator().forEach { e ->
					zipFileSigned.putNextEntry(e)

					val mustBeSigned = filesToSign.any { Pattern.matches(it, e.name) }

					if (mustBeSigned) {
						val fileContentToSign = zipFile.getInputStream(e).readAllBytes()

						val fileToSign = buildDirectory.resolve(e.name).toFile()
						fileToSign.deleteOnExit()
						fileToSign.parentFile.mkdirs()
						fileToSign.createNewFile()

						val fileToSignOutputStream = FileOutputStream(fileToSign)
						fileToSignOutputStream.write(fileContentToSign)
						fileToSignOutputStream.close()

						execProvider.exec {
							commandLine(
								"codesign",
								"-vvv",
								"--display",
								"--strict",
								"--force",
								"--deep",
								"--options=runtime",
								"--timestamp",
								"-s",
								"Developer ID Application: ${signingId.get()}",
								fileToSign.path,
							)
						}

						zipFileSigned.write(fileToSign.inputStream().readAllBytes())
					} else {
						zipFileSigned.write(zipFile.getInputStream(e).readAllBytes())
					}
					zipFileSigned.closeEntry()
				}
				zipFileSigned.finish()

				val signedJarOutputLocation = jarFilesSignedOutputDir.resolve(jarFile.name)
				Files.copy(zipFileSignedPath, signedJarOutputLocation, StandardCopyOption.REPLACE_EXISTING)
			}
		}
	}

tasks.register<MacSignLibrariesTask>("prepareMacBundle") {
	onlyIf("OS is not Mac") {
		Platform.isMac()
	}
	dependsOn("build", "copyDependencies", "copyJar")

	// skip this task if no signingId is configured
	if (macSigningId != null) {
		signingId = macSigningId
	} else {
		enabled = false
	}

	jarFiles.setDir(layout.buildDirectory.dir("jars")).include("jna-*.jar")
	jarFilesSignedOutputDir =
		Path.of(
			layout.buildDirectory
				.dir("jars")
				.get()
				.asFile
				.toURI(),
		)
	filesToSign =
		listOf(
			"com/sun/jna/darwin-.*/libjnidispatch.jnilib",
		)
	compressionLevel = Deflater.BEST_COMPRESSION
}

tasks.register("packageDmg", JPackageTask::class) {
	group = "Distribution"
	description = "Creates a DMG package for installation."

	onlyIf("OS is not Mac") {
		Platform.isMac()
	}
	dependsOn("prepareMacBundle")

	applyDefaults()
	macConfigs()

	type = ImageType.DMG
}

tasks.register("packagePkg", JPackageTask::class) {
	group = "Distribution"
	description = "Creates a PKG package for installation."

	onlyIf("OS is not Mac") {
		Platform.isMac()
	}
	dependsOn("prepareMacBundle")

	applyDefaults()
	macConfigs()

	type = ImageType.PKG
}

tasks.register("packageMac") {
	group = "Distribution"
	description = "Creates DMG and PKG packages for Mac systems."

	dependsOn(
		"packagePkg",
		"packageDmg",
	)
}

// windows packaging

tasks.register("packageMsi", JPackageTask::class) {
	group = "Distribution"
	description = "Creates a MSI package for installation."

	// we need the following for the iscc task which reuses the exe-file from this task
	// to keep it we put it in /build/jpfiles
	// jp will however fail if this dir exists beforehand, so we make sure to delete it...
	doFirst {
		delete(project.layout.buildDirectory.dir("jpfiles"))
	}
	temp = "/build/jpfiles"

	onlyIf("OS is not Windows") {
		Platform.isWindows()
	}
	dependsOn("build", "copyDependencies", "copyJar")

	applyDefaults()
	windowsConfigs()

	type = ImageType.MSI
}

val issWorkDir = layout.buildDirectory.dir("iscc")
tasks.register("prepareIsccFile", Copy::class) {

	val iconPath = projectDir.resolve("src/main/package/win/Open-eCard-App.ico").toString()
	val bmpPath = projectDir.resolve("src/main/package/win/Open-eCard-App-setup-icon.bmp").toString()

	from("src/main/package/win")

	include("Open-eCard-App.iss")
	filter { line ->
		line
			.replace("\$appName", setAppName)
			.replace("\$appVersion", setAppVersion)
			.replace("\$vendor", setAppVendor)
			.replace("\$appUrl", setAppAboutUrl)
			.replace("\$identifier", setAppName)
			.replace("\$licensePath", setAppLicenseFile)
			.replace("\$outPath", "$projectDir\\build\\dist")
			.replace("\$iconFile", iconPath)
			.replace("\$bmpPath", bmpPath)
			.replace("\$msiPath", "$projectDir\\build\\jpfiles\\images\\win-msi.image\\Open-eCard-App")
	}

	into(issWorkDir)
	outputs.upToDateWhen { false }
}

tasks.register("packageExe", Exec::class) {
	group = "Distribution"
	description = "Creates a EXE for installation."

	onlyIf("OS is not Windows") {
		Platform.isWindows()
	}
	dependsOn("build", "copyDependencies", "copyJar", "packageMsi", "prepareIsccFile")

	workingDir(issWorkDir)
	executable("iscc")
	args("Open-eCard-App.iss")
}

tasks.register("packageWindows") {
	group = "Distribution"
	description = "Creates EXE and MSI packages for Windows systems."

	dependsOn(
		"packageExe",
		"packageMsi",
	)
}
