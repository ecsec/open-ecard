description = "wsdef-common"

plugins {
	id("openecard.lib-conventions")
}

dependencies {
	implementation(libs.slf4j.api)
	api(libs.annotations)
	api(libs.jaxb.api)
}
