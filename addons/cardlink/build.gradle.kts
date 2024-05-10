description = "cardlink"

plugins {
	id("openecard.lib-conventions")
}

dependencies {
//	api(libs.jaxb.api)
//	api(libs.jaxb.ws.api)
//	api(libs.slf4j.api)
	api(project(":addon"))
	api(project(":crypto:tls"))
	api(project(":ifd:ifd-common"))
	api(project(":sal:sal-common"))
	api(project(":clients:mobile-lib"))
//	implementation(project(":i18n"))
//	api(libs.bc.prov)
//	api(libs.bc.tls)
//	api(libs.httpcore)

	testImplementation(libs.bundles.test.basics)
//	testImplementation(project(":gui:swing"))
//	testImplementation(project(":ifd:ifd-core"))
//	testImplementation(project(":management"))
//	testImplementation(project(":sal:tiny-sal"))
}
