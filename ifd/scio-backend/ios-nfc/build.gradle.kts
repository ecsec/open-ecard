description = "ios-nfc"

plugins {
	id("openecard.lib-conventions")
}

dependencies {
	implementation(libs.robovm.rt)
	implementation(libs.robovm.cocoa)
//	api(libs.jaxb.api)
//	api(libs.jaxb.ws.api)
//	api(libs.slf4j.api)
//	api(project(":common"))
//	api(project(":ifd:ifd-common"))
	api(project(":ifd:scio-backend:mobile-nfc"))
//	implementation(project(":wsdef:wsdef-client"))
//	implementation(project(":i18n"))
//	api(libs.bc.prov)
//	api(libs.bc.tls)
//	api(libs.httpcore)
//	api(libs.proxyvole)
//	api(project(":gui:graphics"))
//	api(libs.pdfbox)
//	api(libs.scio)

	testImplementation(libs.bundles.test.basics)
}
