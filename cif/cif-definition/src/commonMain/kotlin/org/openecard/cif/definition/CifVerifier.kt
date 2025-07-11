package org.openecard.cif.definition

import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.app.ApplicationDefinition
import org.openecard.cif.definition.dataset.DataSetDefinition
import org.openecard.cif.definition.did.DidDefinition
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.definition.did.GenericCryptoDidDefinition
import org.openecard.cif.definition.did.PaceDidDefinition
import org.openecard.cif.definition.did.PinDidDefinition

class CifVerifier(
	private val cif: CardInfoDefinition,
) {
	@Throws(IllegalArgumentException::class)
	fun verify() {
		// check app uniqueness
		if (cif.applications.distinctBy { it.name }.size != cif.applications.size) {
			throw IllegalArgumentException("Duplicate application names found")
		}
		if (cif.applications.distinctBy { it.aid }.size != cif.applications.size) {
			throw IllegalArgumentException("Duplicate application AIDs found")
		}

		// check DID uniqueness
		val allDids = cif.applications.flatMap { it.dids }
		if (allDids.distinctBy { it.name }.size != allDids.size) {
			throw IllegalArgumentException("Duplicate DID names found in CIF")
		}

		// check dataset uniqueness
		val allDatasets = cif.applications.flatMap { it.dataSets }
		if (allDatasets.distinctBy { it.name }.size != allDatasets.size) {
			throw IllegalArgumentException("Duplicate dataset names found in CIF")
		}

		cif.applications.forEach { a ->
			if (a.dataSets.distinctBy { it.path }.size != a.dataSets.size) {
				throw IllegalArgumentException("Duplicate dataset paths found in application '${a.name}'")
			}
			if (a.dataSets
					.filter { it.shortEf != null }
					.distinctBy { it.shortEf }
					.size != a.dataSets.size
			) {
				throw IllegalArgumentException("Duplicate dataset Short-EFs found in application '${a.name}'")
			}
		}

		// check ACLs and reference existence
		cif.applications.forEach { app ->
			checkAppAcl(app)
			app.dataSets.forEach { ds ->
				checkDatasetAcl(app, ds)
			}
			app.dids.forEach { did ->
				checkDidAcl(app, did)

				when (did) {
					is GenericCryptoDidDefinition<*> -> {
						did.parameters.certificates.forEach { certDs ->
							checkDatasetExists(app, certDs)
						}
					}
					else -> {}
				}
			}
		}
	}

	private fun checkAppAcl(app: ApplicationDefinition) {
		checkAcl(app, "Application.select", app.name, app.selectAcl.acls)
	}

	private fun checkDatasetAcl(
		app: ApplicationDefinition,
		ds: DataSetDefinition,
	) {
		checkAcl(app, "Dataset.read", ds.name, ds.readAcl.acls)
		checkAcl(app, "Dataset.write", ds.name, ds.writeAcl.acls)
	}

	private fun checkDidAcl(
		app: ApplicationDefinition,
		did: DidDefinition,
	) {
		when (did) {
			is GenericCryptoDidDefinition.DecryptionDidDefinition -> {
				checkAcl(app, "Did.decipher", did.name, did.decipherAcl.acls)
			}
			is GenericCryptoDidDefinition.EncryptionDidDefinition -> {
				checkAcl(app, "Did.encipher", did.name, did.encipherAcl.acls)
			}
			is GenericCryptoDidDefinition.SignatureDidDefinition -> {
				checkAcl(app, "Did.sign", did.name, did.signAcl.acls)
			}
			is PaceDidDefinition -> {
				checkAcl(app, "Did.auth", did.name, did.authAcl.acls)
				checkAcl(app, "Did.modify", did.name, did.modifyAcl.acls)
			}
			is PinDidDefinition -> {
				checkAcl(app, "Did.auth", did.name, did.authAcl.acls)
				checkAcl(app, "Did.modify", did.name, did.modifyAcl.acls)
			}
		}
	}

	private fun checkAcl(
		app: ApplicationDefinition,
		type: String,
		name: String,
		acls: Map<CardProtocol, CifAclOr>,
	) {
		require(acls.isNotEmpty()) { "No ACLs provided in $type $name" }

		// check that there are no Always true branches, when there are DID references
		acls.forEach { acl ->
			var hasAlways = false
			var hasRefs = false
			acl.value.or.forEach { ors ->
				hasRefs = hasRefs or ors.and.any { it is DidStateReference }
				hasAlways = hasAlways or (
					// true if only true contained in the list
					ors.and.fold(null as Boolean?) { last, term ->
						when (term) {
							BoolTreeLeaf.True -> last ?: true
							is DidStateReference -> false
						}
					} ?: false
				)
			}
			if (hasAlways && hasRefs) {
				throw IllegalArgumentException(
					"Always true used in combination with DID references in DID '$name' ($type) in application '${app.name}'",
				)
			}
		}

		// check references
		acls.forEach { acl ->
			acl.value.or.forEach { or ->
				or.and.forEach { entry ->
					when (entry) {
						is DidStateReference -> checkDidExists(app, entry.name)
						else -> null
					}
				}
			}
		}
	}

	private fun checkDidExists(
		localApp: ApplicationDefinition,
		name: String,
	) {
		cif.applications.forEach { a ->
			val did = a.dids.find { it.name == name }
			if (did != null) {
				when (did.scope) {
					DidScope.LOCAL ->
						if (a.name == localApp.name) {
							return
						} else {
							throw IllegalArgumentException("Local DID '$name' in app '${a.name}' referenced from app '${localApp.name}'")
						}
					DidScope.GLOBAL -> return
				}
			}
		}

		// nothing found
		throw IllegalArgumentException("DID '$name' not found in any application")
	}

	private fun checkDatasetExists(
		localApp: ApplicationDefinition,
		name: String,
	) {
		if (localApp.dataSets.any { it.name == name }) {
			return
		}

		// nothing found
		throw IllegalArgumentException("Dataset '$name' not found")
	}
}
