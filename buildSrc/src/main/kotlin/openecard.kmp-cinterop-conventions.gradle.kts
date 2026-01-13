import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
	id("openecard.kmp-conventions")
}

tasks.withType<KotlinNativeLink> {
	// disable all link tasks which don't fit the target platform
	enabled = matchesTarget(target)
}

private fun matchesTarget(target: String): Boolean {
	val archStr =
		when {
			Os.isArch("amd64") -> "x64"
			Os.isArch("aarch64") -> "arm64"
			else -> "unknown"
		}

	val possibleTargets =
		if (Os.isFamily(Os.FAMILY_MAC)) {
			listOf("macos_$archStr", "ios_arm64", "ios_simulator_arm64")
		} else if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			listOf("mingw_$archStr")
		} else if (Os.isFamily(Os.FAMILY_UNIX)) {
			listOf("linux_$archStr")
		} else {
			listOf()
		}

	return target in possibleTargets
}
