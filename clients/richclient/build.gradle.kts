import com.sun.jna.Platform
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.util.internal.VersionNumber
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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

description = "Richclient"

plugins {
	id("openecard.app-conventions")
	alias(libs.plugins.jfx)
	alias(libs.plugins.jpackage)
}

javafx {
	version = libs.versions.jfx.get()
	modules = listOf("javafx.swing", "javafx.controls", "javafx.fxml")
}

application {
	mainClass = "org.openecard.richclient.RichClient"
}

val richclientJavaTargetVersion: String by project
kotlin {
	compilerOptions.jvmTarget = JvmTarget.fromTarget(richclientJavaTargetVersion)
}
java {
	val jVersion = JavaVersion.toVersion(richclientJavaTargetVersion)
	sourceCompatibility = jVersion
	targetCompatibility = jVersion
}

class JnaCapability : ComponentMetadataRule {
	override fun execute(context: ComponentMetadataContext) =
		context.details.run {
			if (setOf("jna", "jna-jpms").contains(id.name)) {
				allVariants {
					withCapabilities {
						addCapability("jna", "jna", id.version)
					}
				}
			}
		}
}

class JnaPlatformCapability : ComponentMetadataRule {
	override fun execute(context: ComponentMetadataContext) =
		context.details.run {
			if (setOf("jna-platform", "jna-platform-jpms").contains(id.name)) {
				allVariants {
					withCapabilities {
						addCapability("jna", "jna-platform", id.version)
					}
				}
			}
		}
}

configurations.all {
	resolutionStrategy.capabilitiesResolution.withCapability("jna:jna") {
		val toBeSelected =
			candidates.firstOrNull {
				it.id.let { id ->
					id is ModuleComponentIdentifier &&
						id.module == "jna-jpms"
				}
			}
		if (toBeSelected != null) {
			select(toBeSelected)
		}
		because("use jna jpms module instead of plain jna")
	}
	resolutionStrategy.capabilitiesResolution.withCapability("jna:jna-platform") {
		val toBeSelected =
			candidates.firstOrNull {
				it.id.let { id ->
					id is ModuleComponentIdentifier &&
						id.module == "jna-platform-jpms"
				}
			}
		if (toBeSelected != null) {
			select(toBeSelected)
		}
		because("use jna-platform jpms module instead of plain jna-platform")
	}

	resolutionStrategy {
		eachDependency {
			if (requested.group == "org.apache.xmlgraphics" && requested.name.contains("batik")) {
				useTarget("${requested.group}:${requested.name}:${libs.versions.apache.batik.get()}")
				because("Old versions of batik are not Java module aware")
			}
		}
	}

	// already covered by java.xml
	exclude("xml-apis", "xml-apis")
}

dependencies {
	components.all(JnaCapability::class.java)
	components.all(JnaPlatformCapability::class.java)

	implementation(libs.kotlin.logging)
	implementation(libs.logback.classic)

	implementation(project(":bindings:ktor"))

	// gui
	api(libs.pdfbox)
	api(libs.apache.batik.transcoder)
	implementation(libs.ktor.client.core)

	// addons
	implementation(project(":addons:tr03124"))

	// basic runtime deps
	implementation(project(":clients:richclient-res"))
	implementation(project(":i18n"))
	// implementation(libs.apache.batik)
	implementation(libs.systray)

	implementation(libs.jna.jpms)
	implementation(libs.jna.jpms.platform)

	implementation(project(":releases"))
	implementation(project(":build-info"))

	// Card Stack
	implementation(project(":smartcard:pcsc-native"))
	implementation(project(":sal:smartcard-sal"))
	implementation(project(":cif:bundled-cifs"))
	implementation(project(":smartcard:pace"))

	// JavaFX
	implementation(libs.kotlin.coroutines.javafx)

	// http client
	implementation(libs.ktor.client.core)
	implementation(libs.ktor.client.okhttp)
	// proxy
	implementation(libs.proxyvole)

	// testing
	testImplementation(libs.bundles.test.basics.kotlin)
}

tasks.withType<AbstractTestTask>().configureEach {
	failOnNoDiscoveredTests = false
}

val setAppName = "Open-eCard-App"
val setAppVendor = "ecsec GmbH"
val setAppLicenseFile = projectDir.resolve("../../LICENSE.GPL")
val setAppAboutUrl = "https://openecard.org/"
val setAppVersion =
	VersionNumber.parse(project.version.toString()).let {
		"${it.major}.${it.minor}.${it.micro}"
	}
val macSigningId: String? = System.getenv("MAC_SIGNING_ID")

val copyJars by tasks.registering(Copy::class) {
	from(configurations.runtimeClasspath) {
		exclude { it.name.startsWith("javafx") }
		rename("oec_smartcard_pcsc-native", "oec_smartcard_pcsc-nativelib")
	}.into(layout.buildDirectory.dir("jars"))
}
val copyMods by tasks.registering(Copy::class) {
	from(configurations.runtimeClasspath) {
		include { it.name.startsWith("javafx") }
	}.into(layout.buildDirectory.dir("jars/mods"))
}
val copyJar by tasks.registering(Copy::class) {
	from(tasks.jar).into(layout.buildDirectory.dir("jars"))
}
val copyDependencies by tasks.registering {
	dependsOn(copyJars)
	dependsOn(copyMods)
	dependsOn(copyJar)
}

fun JPackageTask.applyDefaults(imageType: ImageType) {
	verbose = false

	input = layout.buildDirectory.dir("jars")
	mainJar =
		tasks.jar
			.get()
			.archiveFileName
			.get()
	mainClass = application.mainClass.get()

	type = imageType
	destination = layout.buildDirectory.dir("dist/${imageType.name}")
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
			"--module-path",
			"\$APPDIR${File.separator}mods",
			"--add-modules",
			"javafx.swing,javafx.controls,javafx.fxml",
		)
	appName = setAppName
	appVersion = setAppVersion
	vendor = setAppVendor
	if (imageType != ImageType.APP_IMAGE) {
		licenseFile = setAppLicenseFile
		aboutUrl = setAppAboutUrl
	}
	copyright = "Copyright (C) ${LocalDate.now().year} ecsec GmbH"
	appDescription = "Client side implementation of the eCard-API-Framework (BSI TR-03112)"

	// configure icons
	if (Os.isFamily(Os.FAMILY_WINDOWS)) {
		icon = layout.projectDirectory.file("src/main/package/win/Open-eCard-App.ico")
	} else if (Os.isFamily(Os.FAMILY_MAC)) {
		icon = layout.projectDirectory.file("src/main/package/mac/Open-eCard-App.icns")
	} else if (Os.isFamily(Os.FAMILY_UNIX)) {
		icon = layout.projectDirectory.file("src/main/package/linux/Open-eCard-App.png")
	}

	System.getenv("RUNTIME_JDK_PATH")?.let {
		jLinkOptions.add("--modulePath")
		jLinkOptions.add(it)
	}
}

// configs for the packages

fun JPackageTask.linuxConfigs() {
// 	resourceDir = layout.projectDirectory.dir("src/main/package/linux")

	linuxDebMaintainer = "tobias.wich@ecsec.de"
	linuxPackageName = "open-ecard-app"
	linuxAppCategory = "utils"
	// linuxRpmLicenseType = "GPLv3+"
	linuxMenuGroup = "Network"
	// linuxPackageDeps = false
}

fun JPackageTask.windowsConfigs() {
	resourceDir = layout.projectDirectory.dir("src/main/package/win")

	winDirChooser = true
	winMenuGroup = "misc"
	winUpgradeUuid = "B11CB66-71B5-42C1-8076-15F1FEDCC22A"
	winShortcut = true
	winShortcutPrompt = true
}

fun JPackageTask.macConfigs() {
	resourceDir = layout.projectDirectory.dir("src/main/package/mac")

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

// packaging jobs for debugging and manual operations

val buildAppImage by tasks.registering(JPackageTask::class) {
	group = "Distribution"
	description = "Creates a AppImage artifact for further processing."

	onlyIf("OS is not Linux") {
		Platform.isLinux()
	}
	dependsOn(copyDependencies)

	applyDefaults(ImageType.APP_IMAGE)
}

// linux packaging

val packageDeb by tasks.registering(JPackageTask::class) {
	group = "Distribution"
	description = "Creates a DEB package for installation."

	onlyIf("OS is not Linux") {
		Platform.isLinux()
	}
	dependsOn(copyDependencies)

	applyDefaults(ImageType.DEB)
	linuxConfigs()
}

val packageRpm by tasks.registering(JPackageTask::class) {
	group = "Distribution"
	description = "Creates a RPM package for installation."

	onlyIf("OS is not Linux") {
		Platform.isLinux()
	}
	dependsOn(copyDependencies)

	applyDefaults(ImageType.RPM)
	linuxConfigs()
}

val packageLinux by tasks.registering {
	group = "Distribution"
	description = "Creates DEB and RPM packages for linux systems."

	dependsOn(
		packageDeb,
		packageRpm,
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

val prepareMacBundle by tasks.registering(MacSignLibrariesTask::class) {
	onlyIf("OS is not Mac") {
		Platform.isMac()
	}
	dependsOn(copyDependencies)

	// skip this task if no signingId is configured
	if (macSigningId != null) {
		signingId = macSigningId
	} else {
		enabled = false
	}

	jarFiles
		.setDir(layout.buildDirectory.dir("jars"))
		.include(
			"jna-*.jar",
			"jansi-*.jar",
		)
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
			"org/fusesource/jansi/internal/native/Mac/.*/libjansi.jnilib",
		)
	compressionLevel = Deflater.BEST_COMPRESSION
}

val packageDmg by tasks.registering(JPackageTask::class) {
	group = "Distribution"
	description = "Creates a DMG package for installation."

	onlyIf("OS is not Mac") {
		Platform.isMac()
	}
	dependsOn(prepareMacBundle)

	applyDefaults(ImageType.DMG)
	macConfigs()
}

val packagePkg by tasks.registering(JPackageTask::class) {
	group = "Distribution"
	description = "Creates a PKG package for installation."

	onlyIf("OS is not Mac") {
		Platform.isMac()
	}
	dependsOn(prepareMacBundle)

	applyDefaults(ImageType.PKG)
	macConfigs()
}

tasks.register("packageMac") {
	group = "Distribution"
	description = "Creates DMG and PKG packages for Mac systems."

	dependsOn(
		packagePkg,
		packageDmg,
	)
}

// windows packaging

val packageMsi by tasks.registering(JPackageTask::class) {
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
	dependsOn(copyDependencies)

	applyDefaults(ImageType.MSI)
	windowsConfigs()
}

val issWorkDir = layout.buildDirectory.dir("iscc")
val prepareIsccFile by tasks.registering(Copy::class) {

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
			.replace("\$licensePath", setAppLicenseFile.path)
			.replace("\$outPath", "$projectDir\\build\\dist\\EXE")
			.replace("\$iconFile", iconPath)
			.replace("\$bmpPath", bmpPath)
			.replace("\$msiPath", "$projectDir\\build\\jpfiles\\image\\Open-eCard-App")
	}

	into(issWorkDir)
	outputs.upToDateWhen { false }
}

val packageExe by tasks.registering(Exec::class) {
	group = "Distribution"
	description = "Creates a EXE for installation."

	onlyIf("OS is not Windows") {
		Platform.isWindows()
	}
	dependsOn(copyDependencies, packageMsi, prepareIsccFile)

	workingDir(issWorkDir)
	executable("iscc")
	args("Open-eCard-App.iss")
}

tasks.register("packageWindows") {
	group = "Distribution"
	description = "Creates EXE and MSI packages for Windows systems."

	dependsOn(
		packageExe,
		packageMsi,
	)
}
