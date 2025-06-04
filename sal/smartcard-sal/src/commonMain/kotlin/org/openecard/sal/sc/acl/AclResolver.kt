package org.openecard.sal.sc.acl

import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.NeverTree
import org.openecard.cif.definition.ordinal
import org.openecard.sc.iface.CardProtocol

fun AclDefinition.validForProtocol(protocol: CardProtocol): Boolean = selectForProtocol(protocol).hasSolution()

fun AclDefinition.selectForProtocol(protocol: CardProtocol): BoolTreeOr<BoolTreeLeaf> {
	val acl =
		this.acls
			.toList()
			.sortedBy { it.first.ordinal() }
			.firstOrNull {
				when (it.first) {
					org.openecard.cif.definition.CardProtocol.Any -> true
					org.openecard.cif.definition.CardProtocol.Grouped.CONTACT -> protocol.isContact
					org.openecard.cif.definition.CardProtocol.Grouped.CONTACTLESS -> protocol.isContactLess
					org.openecard.cif.definition.CardProtocol.Technical.T0 -> protocol == CardProtocol.T0
					org.openecard.cif.definition.CardProtocol.Technical.T1 -> protocol == CardProtocol.T1
					org.openecard.cif.definition.CardProtocol.Technical.T15 -> protocol == CardProtocol.T15
					org.openecard.cif.definition.CardProtocol.Technical.TCL -> protocol == CardProtocol.TCL
				}
			}
	return acl?.second ?: NeverTree
}

// TODO: better look for a branch which has true and/or did references
fun CifAclOr.hasSolution(): Boolean = this != NeverTree
