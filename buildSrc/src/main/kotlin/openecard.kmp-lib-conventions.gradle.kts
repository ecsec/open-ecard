import kotlin.text.replace

plugins {
	id("openecard.kmp-conventions")
	id("openecard.publish-conventions")
}

base {
	// change archive name of libraries, so we don't run into issues when copying to libs folders
	val path = project.path.replace(":", "_")
	archivesName = "oec$path"
}
