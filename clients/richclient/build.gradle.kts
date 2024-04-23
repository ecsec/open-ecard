import com.android.build.gradle.internal.tasks.factory.registerTask
import com.sun.jna.Platform
import org.apache.tools.ant.filters.ReplaceTokens
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
//	api(libs.jaxb.api)
//	api(libs.jaxb.ws.api)
//	api(libs.slf4j.api)
	implementation(libs.logback.classic)
//	api(project(":addon"))
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

//	api(libs.bc.prov)
//	api(libs.bc.tls)
//	api(libs.httpcore)
	implementation(libs.jose4j)
	implementation(libs.jna.jpms)
	implementation(libs.jna.jpms.platform)

	testImplementation(libs.bundles.test.basics)
//	testImplementation(project(":gui:swing"))
//	testImplementation(project(":ifd:ifd-core"))
//	testImplementation(project(":management"))
//	testImplementation(project(":sal:tiny-sal"))
}


//task("copyDependencies", Copy::class) {
//	from(configurations.runtimeClasspath).into(layout.buildDirectory.dir("jmods"))
//}
//task("copyJar", Copy::class) {
//	from(tasks.jar).into(layout.buildDirectory.dir("jmods"))
//}

task("copyDependencies", Copy::class) {
	from(configurations.runtimeClasspath).into(layout.buildDirectory.dir("jars"))
}
task("copyJar", Copy::class) {
	from(tasks.jar).into(layout.buildDirectory.dir("jars"))
}

//<app.identifier>Open-eCard-App</app.identifier>
//<app.maintainer>tobias.wich@ecsec.de</app.maintainer>
//<app.url>https://openecard.org</app.url>
//<app.description>Client side implementation of the eCard-API-Framework (BSI TR-03112)</app.description>
//
//<package.identifier>open-ecard-app</package.identifier>
//<package.deps>pcscd</package.deps>
//<package.type>PLATFORM</package.type>

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
tasks.register("packageDmg", JPackageTask::class){
	group = "Distribution"
	description = "Creates a DMG package for installation."

	assert(Platform.isMac())
	dependsOn("build", "copyDependencies", "copyJar")

	applyDefaults()
	macConfigs()

	type = ImageType.DMG
}
tasks.register("packagePkg", JPackageTask::class){
	group = "Distribution"
	description = "Creates a PKG package for installation."

	assert(Platform.isMac())
	dependsOn("build", "copyDependencies", "copyJar")

	applyDefaults()
	macConfigs()

	type = ImageType.PKG
}
tasks.register("packageMsi", JPackageTask::class){
	group = "Distribution"
	description = "Creates a MSI package for installation."

	assert(Platform.isWindows())
	dependsOn("build", "copyDependencies", "copyJar")

	temp = "/build/jpfiles"

	applyDefaults()
	windowsConfigs()

	type = ImageType.MSI
}

val issWorkDir = layout.buildDirectory.dir("iscc")
tasks.register("prepareIsccFile", Copy::class){

	val version = VersionNumber.parse(project.version.toString()).let {
		"${it.major}.${it.minor}.${it.micro}"
	}
	val appName = "Open-eCard-App"
	val vendor = "ecsec GmbH"
	val licenseFile = projectDir.resolve("../../LICENSE.GPL").path
	val aboutUrl = "https://openecard.org/"
	val iconPath = projectDir.resolve("src/main/package/win/Open-eCard-App.ico").toString()
	val bmpPath = projectDir.resolve("src/main/package/win/Open-eCard-App-setup-icon.bmp").toString()

	from("src/main/package/win")

	include("Open-eCard-App.iss")
	filter {line ->
		line.replace("\$appName", appName)
		.replace("\$appVersion" , version)
		.replace("\$vendor" , vendor)
		.replace("\$url" , aboutUrl)
		.replace("\$identifier" , appName)
		.replace("\$licensePath" , licenseFile)
		.replace("\$outPath" , "$projectDir\\build\\dist")
		.replace("\$iconFile" , iconPath)
		.replace("\$bmpPath" , bmpPath)
		.replace("\$msiPath" , "$projectDir\\build\\jpfiles\\images\\win-msi.image\\Open-eCard-App")
	}

	into(issWorkDir)
	outputs.upToDateWhen { false }
}

tasks.register("packageExeIscc", Exec::class){
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
		"packageExeIscc",
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


	appName = "Open-eCard-App"
	VersionNumber.parse(project.version.toString()).let {
		appVersion = "${it.major}.${it.minor}.${it.micro}"
	}
	appDescription = "Client side implementation of the eCard-API-Framework (BSI TR-03112)"
	vendor = "ecsec GmbH"
	copyright = "Copyright (C) ${LocalDate.now().year} ecsec GmbH"
	licenseFile = projectDir.resolve("../../LICENSE.GPL").path
	aboutUrl = "https://openecard.org/"
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
	macSigningKeyUserName = "ecsec GmbH (72RMQ6K75Z)"
	macSigningKeychain = "/Users/ecsec-ci/Library/Keychains/ecsec.keychain-db"

}
