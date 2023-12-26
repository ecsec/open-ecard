description = "jaxb-marshaller"

plugins {
	id("openecard.lib-conventions")
}

dependencies {
	implementation(libs.slf4j.api)
	api(libs.jaxb.api)
	implementation(libs.jaxb.impl)
	implementation(project(":wsdef:wsdef-common"))

	testImplementation(libs.bundles.test.basics)
	testImplementation(project(":wsdef:wsdef-client"))
}
