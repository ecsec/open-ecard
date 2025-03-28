import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.constraints.Constraint
import io.github.z4kn4fein.semver.constraints.toConstraint
import io.github.z4kn4fein.semver.satisfiesAny
import io.github.z4kn4fein.semver.toVersion
import io.github.z4kn4fein.semver.toVersionOrNull
import org.eclipse.jgit.api.Git
import org.jose4j.json.JsonUtil
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import java.net.URI
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*


tasks.register<ReleaseInfoTask>("buildReleaseInfo") {
	releaseInfoFile.set(layout.buildDirectory.file("release-info.jwt"))
	artifactHashesFile.set(layout.buildDirectory.file("artifacts.sha256sum"))
	currentVersion.set(project.version.toString())
	currentVersionIsLatest.set(false)
}

abstract class ReleaseInfoTask: DefaultTask() {

	@get:InputFile
	val verificationKeyFile: RegularFileProperty = project.objects.fileProperty()

	@get:InputFile
	val versionStatusFile: RegularFileProperty = project.objects.fileProperty()

	@get:InputFile
	val artifactHashesFile: RegularFileProperty = project.objects.fileProperty()

	@get:OutputFile
	val releaseInfoFile: RegularFileProperty = project.objects.fileProperty()

	@get:Input
	var jwtIssuer = "https://openecard.org"
	@get:Input
	var jwtAudience = "https://openecard.org/app"

	@get:Input
	var releaseBaseUrl = "https://github.com/ecsec/open-ecard/releases"
	@get:Input
	var releaseJwtPath = "release-info.jwt"

	@get:Input
	val currentVersion: Property<String> = project.objects.property()

	@get:Input
	val currentVersionIsLatest: Property<Boolean> = project.objects.property()

	@TaskAction
	fun execute() {
		val signKey = System.getenv("RELEASE_SIGN_KEY") ?: error("RELEASE_SIGN_KEY not set")

		val releaseInfoJson = buildReleaseInfo()

		val jwt = signReleaseInfo(releaseInfoJson, signKey)
		releaseInfoFile.get().asFile.writeText(jwt)
	}

	private fun buildReleaseInfo(): Map<String, Any> {
		val versionStatus = JsonUtil.parseJson(versionStatusFile.get().asFile.readText())
		val maintainConstraints = getMaintainConstraints(versionStatus)

		val curVersion = currentVersion.get().toVersion()
		val currentArtifacts = getCurrentArtifacts(curVersion)

		val tagVersions = getLatestGitReleaseVersions(curVersion)
		logger.info("Found versions in git tags: ${tagVersions.joinToString(", ")}")
		val latestVersion = tagVersions.first()
		val isMaintenanceRelease = latestVersion != curVersion
		val latestVersionData = getLatestVersionData(isMaintenanceRelease, latestVersion, currentArtifacts)
		val maintainedVersions = getLatestMaintainedVersions(curVersion, tagVersions, maintainConstraints)
		var maintenanceVersions = loadMaintenanceInfos(maintainedVersions)
		if (isMaintenanceRelease) {
			maintenanceVersions += latestVersionData
		}

		val releaseInfo = mapOf(
			"version" to curVersion,
			"latestVersion" to latestVersionData.toJson(),
			"maintenanceVersions" to maintenanceVersions.map { it.toJson()},
			"artifacts" to currentArtifacts,
			"versionStatus" to versionStatus,
		)

		return releaseInfo
	}

	private fun getLatestVersionData(maintenanceRelease: Boolean, latestVersion: Version, currentArtifacts: List<Map<String, String>>): VersionData {
		return if (! maintenanceRelease) {
			VersionData(
				latestVersion,
				currentArtifacts,
			)
		} else {
			logger.info("Loading latest version ($latestVersion) data from github as we are in a maintenance release.")
			loadVersionData(latestVersion)
		}
	}

	private fun loadMaintenanceInfos(maintainedVersions: List<Version>): List<VersionData> {
		return maintainedVersions.map {
			logger.info("Loading data from github for maintenance release $it")
			loadVersionData(it)
		}
	}

	private fun loadVersionData(version: Version): VersionData {
		// https://github.com/ecsec/open-ecard/releases/download/v2.3.5/release-info.json
		val uri = URI.create("$releaseBaseUrl/download/v$version/$releaseJwtPath")
		logger.info("Retrieving release info from $uri")
		val jwt = uri.toURL().readText()
		val releaseInfo = verifyReleaseInfoJwt(jwt)
		@Suppress("UNCHECKED_CAST")
		val artifacts = (releaseInfo["artifacts"] ?: throw IllegalArgumentException("Missing artifacts in release info")) as List<Map<String, Any>>

		return VersionData(
			version,
			artifacts,
		)
	}

	private fun getMaintainConstraints(versionStatus: Map<String, Any>): List<Constraint> {
		when (val maintainedList = versionStatus["maintained"]) {
			is List<*> -> {
				return maintainedList.map {
					it.toString().toConstraint()
				}
			}
			else -> throw IllegalArgumentException("Invalid maintained list")
		}
	}

	private fun getLatestGitReleaseVersions(curVersion: Version): List<Version> {
		return Git.open(project.rootDir).use { git ->
			val tags = git.repository.refDatabase.getRefsByPrefix("refs/tags/v")
			val versions: List<Version> = tags.mapNotNull {
				val versionStr = it.name.removePrefix("refs/tags/v")
				versionStr.toVersionOrNull()
			}.filter {
				it.isStable
			}.sortedDescending()

			val latestVersions: List<Version> = versions.fold(mutableListOf()) { acc, version ->
				when (val last = acc.lastOrNull()) {
					null -> acc.add(version)
					else -> {
						if (last.major == version.major && last.minor == version.minor) {
							// only add latest of x.y versions
						} else {
							acc.add(version)
						}
					}
				}
				acc
			}

			when (currentVersionIsLatest.get()) {
				false -> latestVersions
				true -> latestVersions.filter { it <= curVersion }
			}
		}
	}

	private fun getLatestMaintainedVersions(
		curVersion: Version,
		tagVersions: List<Version>,
		maintainConstraints: List<Constraint>
	): List<Version> {
		val maintainedVersions = tagVersions.filter {
			it.satisfiesAny(maintainConstraints)
		}.filter { it != curVersion }
		return maintainedVersions
	}

	private fun getCurrentArtifacts(version: Version): List<Map<String, String>> {
		val hashData = artifactHashesFile.get().asFile.readLines()
			.filter { it.isNotBlank() }
			.map {
				val parts = "([a-f0-9]{64}) {2}(.+)$".toRegex().matchEntire(it.trim())?.groupValues ?: throw IllegalArgumentException("Invalid hash line")
				val hash = parts[1]
				val fileName = parts[2]
				val suffix = ".*\\.(\\w+)$".toRegex().matchEntire(fileName)?.groupValues?.get(1)
					?: "OTHER"
				mutableMapOf(
					"sha256" to hash,
					"url" to "$releaseBaseUrl/download/v$version/$fileName",
					"type" to suffix.uppercase(Locale.US),
				)
			}

		return hashData.toList()
	}

	private fun verifyReleaseInfoJwt(jwt: String): Map<String, Any> {
		val jwtConsumer = JwtConsumerBuilder()
			.setExpectedIssuer(jwtIssuer)
			.setExpectedAudience(jwtAudience)
			.setRequireIssuedAt()
			.setVerificationKey(readX509PublicKey(verificationKeyFile.get().asFile))
			.setJwsAlgorithmConstraints(
				AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256
			)
			.build()
		val claims = jwtConsumer.processToClaims(jwt)
		val releaseClaim = claims.getClaimValue("release-info")
		@Suppress("UNCHECKED_CAST")
		return when (releaseClaim) {
			is Map<*, *> -> releaseClaim as Map<String, Any>
		else ->
			throw IllegalArgumentException("Invalid release-info claim")
		}
	}

	private fun signReleaseInfo(releaseInfoJson: Map<String, Any>, signKey: String): String {
		val claims = JwtClaims()
		claims.issuer = jwtIssuer
		claims.setAudience(jwtAudience)
		claims.setClaim("release-info", releaseInfoJson)
		claims.setIssuedAtToNow()

		val signer = JsonWebSignature()
		signer.headers.setStringHeaderValue("typ", "JWT")
		signer.payload = claims.toJson()
		signer.key = readPKCS8PrivateKey(signKey)
		signer.algorithmHeaderValue = AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256

		return signer.compactSerialization
	}

	private fun readPKCS8PrivateKey(keyData: String): PrivateKey {
		val privateKeyPEM = keyData
			.replace("-----BEGIN PRIVATE KEY-----", "")
			.replace(System.lineSeparator().toRegex(), "")
			.replace("-----END PRIVATE KEY-----", "")

		val encoded: ByteArray = Base64.getDecoder().decode(privateKeyPEM)

		val keyFactory = KeyFactory.getInstance("EC")
		val keySpec = PKCS8EncodedKeySpec(encoded)
		return keyFactory.generatePrivate(keySpec)
	}

	private fun readX509PublicKey(file: File): PublicKey {
		val key = file.readText()

		val publicKeyPEM = key
			.replace("-----BEGIN PUBLIC KEY-----", "")
			.replace(System.lineSeparator().toRegex(), "")
			.replace("-----END PUBLIC KEY-----", "")

		val encoded: ByteArray = Base64.getDecoder().decode(publicKeyPEM)

		val keyFactory = KeyFactory.getInstance("EC")
		val keySpec = X509EncodedKeySpec(encoded)
		return keyFactory.generatePublic(keySpec)
	}
}

class VersionData(
	val version: Version,
	val artifacts: List<Any>,
) {
	fun toJson(): Map<String, Any> {
		return mapOf(
			"version" to version.toString(),
			"artifacts" to artifacts,
		)
	}
}
