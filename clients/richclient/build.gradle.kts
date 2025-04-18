import com.sun.jna.Platform
import org.gradle.util.internal.VersionNumber
import org.panteleyev.jpackage.ImageType
import org.panteleyev.jpackage.JPackageTask
import java.time.LocalDate

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
	implementation(libs.kotlin.logging)
	implementation(libs.logback.classic)
	implementation(project(":crypto:tls"))

	implementation(project(":ifd:ifd-core"))
	implementation(project(":ifd:scio-backend:pcsc"))
	implementation(project(":ifd:ifd-protocols:pace"))
	implementation(project(":sal:tiny-sal"))
	implementation(project(":management"))
	implementation(project(":bindings:http"))
	implementation(project(":gui:about"))

	// addons
	implementation(project(":addons:chipgateway"))
	implementation(project(":addons:genericcryptography"))
	implementation(project(":addons:pin-management"))
	implementation(project(":addons:status"))
	implementation(project(":addons:tr03112"))

	// basic runtime deps
	implementation(project(":i18n"))
	implementation(project(":cifs"))
	implementation(project(":wsdef:jaxb-marshaller"))

	implementation(project(":releases"))

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
val setAppVersion = VersionNumber.parse(project.version.toString()).let {
	"${it.major}.${it.minor}.${it.micro}"
}
val macSigningId = System.getenv("MAC_SIGNING_ID")

task("copyDependencies", Copy::class) {
	from(configurations.runtimeClasspath).into(layout.buildDirectory.dir("jars"))
}
task("copyJar", Copy::class) {
	from(tasks.jar).into(layout.buildDirectory.dir("jars"))
}

fun JPackageTask.applyDefaults(){
	input  = layout.buildDirectory.dir("jars").get().toString()
	mainJar = tasks.jar.get().archiveFileName.get()
	mainClass = application.mainClass.get()

	destination = layout.buildDirectory.dir("dist").get().toString()
	javaOptions = listOf(
		"-XX:-UsePerfData", "-XX:-Inline",
		"-Xms16m", "-Xmx64m",
		"-XX:+UseG1GC", "-XX:MinHeapFreeRatio=1", "-XX:MaxHeapFreeRatio=5", "-XX:G1ReservePercent=5",
		"-Djavax.xml.stream.isSupportingExternalEntities=false", "-Djavax.xml.stream.supportDTD=false",
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
fun JPackageTask.linuxConfigs() {
//	resourceDir = layout.projectDirectory.dir("src/main/package/linux").toString()
	icon = layout.projectDirectory.dir("src/main/package/linux/Open-eCard-App.png").toString()
	linuxDebMaintainer = "tobias.wich@ecsec.de"
	linuxPackageName = "open-ecard-app"
	linuxAppCategory = "utils"
//	linuxRpmLicenseType = "GPLv3+"
	linuxMenuGroup = "Network"
//	linuxPackageDeps = false
}
fun JPackageTask.windowsConfigs(){
	resourceDir = layout.projectDirectory.dir("src/main/package/win").toString()

	icon = layout.projectDirectory.dir("src/main/package/win/Open-eCard-App.ico").toString()

	winDirChooser = true
	winMenuGroup = "misc"
	winUpgradeUuid = "B11CB66-71B5-42C1-8076-15F1FEDCC22A"
	winShortcut = true
	winShortcutPrompt = true
}
fun JPackageTask.macConfigs(){

	resourceDir = layout.projectDirectory.dir("src/main/package/mac").toString()

	icon = layout.projectDirectory.dir("src/main/package/mac/Open-eCard-App.icns").toString()
	macPackageName = "Open-eCard-App"
	macPackageIdentifier = "org.openecard.versioncheck.MainLoader"

	macSign = true
	macSigningKeyUserName = macSigningId
		?: throw IllegalStateException("Please provide a signing id (Apple TeamID) via the env variable 'MAC_SIGNING_ID'.")
	macSigningKeychain = System.getenv("MAC_SIGNING_KEYCHAIN")
		?: throw IllegalStateException("Please provide a signing keychain via the env variable 'MAC_SIGNING_KEYCHAIN'.")

}

tasks.register("packageDeb", JPackageTask::class){
	group = "Distribution"
	description = "Creates a DEB package for installation."

	assert(Platform.isLinux())
	dependsOn("build", "copyDependencies", "copyJar")

	applyDefaults()
	linuxConfigs()

	type = ImageType.DEB
}
tasks.register("packageRpm", JPackageTask::class){
	group = "Distribution"
	description = "Creates a RPM package for installation."

	assert(Platform.isLinux())
	dependsOn("build", "copyDependencies", "copyJar")

	applyDefaults()
	linuxConfigs()

	type = ImageType.RPM
}
tasks.register("prepareMacBundle") {
    dependsOn("build", "copyDependencies", "copyJar")
    outputs.upToDateWhen { false }
    outputs.cacheIf { false }

    doLast {
        // The JNA libraries are not signed, so we're signing them as soon as all JARS are copied to the build folder
        // If we need to sign in future additional libraries, this can also be done here
        val jnaFiles = fileTree("build/jars") { include("jna-*.jar") }
        for (jnaFile in jnaFiles) {
            exec { commandLine("jar", "xf", jnaFile.path, "com/sun/jna/darwin-x86-64/libjnidispatch.jnilib", "com/sun/jna/darwin-aarch64/libjnidispatch.jnilib") }

            val jnaLibFiles = fileTree("com/sun/jna/") { include("darwin-*/libjnidispatch.jnilib") }
            for (jnaLibFile in jnaLibFiles) {
                val relativeFilePath = jnaLibFile.relativeTo(project.projectDir).path
                exec { commandLine(
                    "codesign",
                    "-vvv",
                    "--display",
                    "--strict",
                    "--force",
                    "--deep",
                    "--options=runtime",
                    "--timestamp",
                    "-s",
                    "Developer ID Application: $macSigningId",
                    relativeFilePath
                ) }
                exec { commandLine("jar", "uf", jnaFile.path, relativeFilePath) }
                jnaLibFile.delete()
            }
        }
    }
}
tasks.register("packageDmg", JPackageTask::class){
	group = "Distribution"
	description = "Creates a DMG package for installation."

	assert(Platform.isMac())
	dependsOn("prepareMacBundle")

	applyDefaults()
	macConfigs()

	type = ImageType.DMG
}
tasks.register("packagePkg", JPackageTask::class){
	group = "Distribution"
	description = "Creates a PKG package for installation."

	assert(Platform.isMac())
	dependsOn("prepareMacBundle")

	applyDefaults()
	macConfigs()

	type = ImageType.PKG
}
tasks.register("packageMsi", JPackageTask::class){
	group = "Distribution"
	description = "Creates a MSI package for installation."

	// we need the following for the iscc task which reuses the exe-file from this task
	// to keep it we put it in /build/jpfiles
	// jp will however fail if this dir exists beforehand, so we make sure to delete it...
	doFirst {
		delete(project.layout.buildDirectory.dir("jpfiles"))
	}
	temp = "/build/jpfiles"

	assert(Platform.isWindows())
	dependsOn("build", "copyDependencies", "copyJar")


	applyDefaults()
	windowsConfigs()

	type = ImageType.MSI
}
val issWorkDir = layout.buildDirectory.dir("iscc")
tasks.register("prepareIsccFile", Copy::class){

	val iconPath = projectDir.resolve("src/main/package/win/Open-eCard-App.ico").toString()
	val bmpPath = projectDir.resolve("src/main/package/win/Open-eCard-App-setup-icon.bmp").toString()

	from("src/main/package/win")

	include("Open-eCard-App.iss")
	filter {line ->
		line.replace("\$appName", setAppName)
		.replace("\$appVersion" , setAppVersion)
		.replace("\$vendor" , setAppVendor)
		.replace("\$appUrl" , setAppAboutUrl)
		.replace("\$identifier" , setAppName)
		.replace("\$licensePath" , setAppLicenseFile)
		.replace("\$outPath" , "$projectDir\\build\\dist")
		.replace("\$iconFile" , iconPath)
		.replace("\$bmpPath" , bmpPath)
		.replace("\$msiPath" , "$projectDir\\build\\jpfiles\\images\\win-msi.image\\Open-eCard-App")
	}

	into(issWorkDir)
	outputs.upToDateWhen { false }
}
tasks.register("packageExe", Exec::class){
	group = "Distribution"
	description = "Creates a EXE for installation."

	assert(Platform.isWindows())
	dependsOn("build", "copyDependencies", "copyJar", "packageMsi", "prepareIsccFile")

	workingDir(issWorkDir)
	executable("iscc")
	args("Open-eCard-App.iss")


}

tasks.register("packageLinux"){
	group = "Distribution"
	description = "Creates DEB and RPM packages for linux systems."

	dependsOn(
		"packageDeb",
		"packageRpm",
	)
}
tasks.register("packageWindows"){
	group = "Distribution"
	description = "Creates EXE and MSI packages for Windows systems."

	dependsOn(
		"packageExe",
		"packageMsi",
	)
}
tasks.register("packageMac"){
	group = "Distribution"
	description = "Creates DMG and PKG packages for Mac systems."

	dependsOn(
		"packagePkg",
		"packageDmg",
	)
}

