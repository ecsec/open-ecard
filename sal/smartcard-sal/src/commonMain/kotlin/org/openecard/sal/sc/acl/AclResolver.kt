package org.openecard.sal.sc.acl

import org.openecard.cif.definition.acl.BoolTreeAnd
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.sal.iface.AclDidResolution
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.hasSolution
import org.openecard.sal.iface.isTrue
import org.openecard.sal.iface.reduceWithAuthenticatedDids
import org.openecard.sal.iface.removeUnsupportedDids
import org.openecard.sal.sc.SmartcardDeviceConnection
import org.openecard.utils.common.returnIf

fun CifAclOr.missingAuthentications(dev: SmartcardDeviceConnection): MissingAuthentications {
	val allDids = dev.allAuthDids
	val aclOnlySupported = this.removeUnsupportedDids(allDids.map { it.name })

	if (!aclOnlySupported.hasSolution()) {
		return MissingAuthentications.Unsolveable
	} else {
		val reduced =
			aclOnlySupported.reduceWithAuthenticatedDids(
				dev.cardState.authenticatedDids
					.map { it.toStateReference() }
					.toSet(),
			)

		if (reduced.isTrue()) {
			return MissingAuthentications.MissingDidAuthentications(BoolTreeOr(listOf()))
		} else {
			val mapped: BoolTreeOr<AclDidResolution> =
				BoolTreeOr(
					reduced.or.mapNotNull { ands ->
						if (ands.and.any { it is BoolTreeLeaf.False }) {
							null
						} else {
							val andBranch =
								ands.and.mapNotNull { term ->
									when (term) {
										BoolTreeLeaf.True -> null
										is DidStateReference -> {
											val did =
												dev.findAuthDid(term.name)
													?: throw IllegalStateException("DID referenced in ACL is not available")
											AclDidResolution(did, term)
										}
										else -> throw IllegalStateException("False detected where there should be none")
									}
								}
							andBranch.returnIf { it.isNotEmpty() }?.let { BoolTreeAnd(it) }
						}
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
