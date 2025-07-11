package org.openecard.sal.iface

import org.openecard.cif.definition.acl.BoolTreeAnd
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.sal.iface.dids.AuthenticationDid
import org.openecard.utils.common.returnIf

fun CifAclOr.missingAuthentications(
	allDids: List<AuthenticationDid>,
	solvedDids: Set<DidStateReference>,
): MissingAuthentications {
	val aclOnlySupported = this.removeUnsupportedDids(allDids.map { it.name })

	if (!aclOnlySupported.hasSolution()) {
		return MissingAuthentications.Unsolveable
	} else {
		val reduced =
			aclOnlySupported.reduceWithAuthenticatedDids(solvedDids)

		if (reduced.isTrue()) {
			return MissingAuthentications.MissingDidAuthentications(BoolTreeOr(listOf()))
		} else {
			val mapped: BoolTreeOr<AclDidResolution> =
				BoolTreeOr(
					reduced.or.mapNotNull { ands ->
						val andBranch =
							ands.and.mapNotNull { term ->
								when (term) {
									BoolTreeLeaf.True -> null
									is DidStateReference -> {
										val did =
											allDids.findAuthDid(term.name)
												?: throw IllegalStateException("DID referenced in ACL is not available")
										AclDidResolution(did, term)
									}
								}
							}
						// remove this branch if there are no entries in it
						andBranch.returnIf { it.isNotEmpty() }?.let { BoolTreeAnd(it) }
					},
				)

			return if (mapped.or.isEmpty()) {
				MissingAuthentications.Unsolveable
			} else {
				MissingAuthentications.MissingDidAuthentications(mapped)
			}
		}
	}
}

private fun List<AuthenticationDid>.findAuthDid(name: String) = find { it.name == name }
