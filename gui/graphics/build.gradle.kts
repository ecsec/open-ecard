description = "graphics"

plugins {
	id("openecard.lib-conventions")
}

dependencies {
//	api(libs.jaxb.api)
//	api(libs.jaxb.ws.api)
	api(libs.slf4j.api)
//	api(project(":crypto:crypto-common"))
//	implementation(project(":wsdef:wsdef-client"))
//	implementation(project(":i18n"))
//	api(libs.bc.prov)
//	api(libs.bc.tls)
//	api(libs.httpcore)
//	api(libs.proxyvole)

	testImplementation(libs.bundles.test.basics)
}
