description = "sal-common"

plugins {
	id("openecard.lib-conventions")
}

dependencies {
//	api(libs.jaxb.api)
//	api(libs.jaxb.ws.api)
//	api(libs.slf4j.api)
	api(project(":common"))
	api(project(":addon"))
	api(project(":crypto:crypto-common"))
//	implementation(project(":i18n"))
//	api(libs.bc.prov)
//	api(libs.bc.tls)
//	api(libs.httpcore)

	testImplementation(libs.bundles.test.basics)
	testImplementation(project(":wsdef:jaxb-marshaller"))
	testImplementation(project(":cifs"))
}
