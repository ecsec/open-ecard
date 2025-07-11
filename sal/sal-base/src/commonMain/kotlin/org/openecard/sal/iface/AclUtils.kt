package org.openecard.sal.iface

import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.acl.AlwaysTree
import org.openecard.cif.definition.acl.BoolTreeAnd
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
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

fun CifAclOr.hasSolution(): Boolean =
	this.or.any { ands ->
		val and = ands.and
		val hasFalse = and.contains(BoolTreeLeaf.False)
		val hasTrue = and.contains(BoolTreeLeaf.True)
		val hasReference = and.any { it is DidStateReference }
		!hasFalse && (hasTrue || hasReference)
	}

fun CifAclOr.isTrue(): Boolean =
	this.simplify().or.any {
		val and = it.and
		// only one true is in this branch
		and.size == 1 && and.first() == BoolTreeLeaf.True
	}

fun CifAclOr.removeUnsupportedDids(supportedDids: List<String>): CifAclOr =
	BoolTreeOr(
		this.or.map { ands ->
			if (ands.and.filterIsInstance<DidStateReference>().any { it.name !in supportedDids }) {
				// remove this branch
				BoolTreeAnd(listOf(BoolTreeLeaf.False))
			} else {
				ands
			}
		},
	).simplify()

fun CifAclOr.reduceWithAuthenticatedDids(solvedDids: Set<DidStateReference>): CifAclOr =
	BoolTreeOr(
		this.or.map { ands ->
			val newAnd =
				ands.and.map { term ->
					when (term) {
						is DidStateReference -> {
							if (solvedDids.any { it.matches(term) }) {
								BoolTreeLeaf.True
							} else {
								term
							}
						}
						else -> term
					}
				}
			BoolTreeAnd(newAnd)
		},
	).simplify()

/**
 * Simplify boolean AND sequence as far as possible.
 */
fun List<BoolTreeLeaf>.simplify(): List<BoolTreeLeaf> =
	if (this.contains(BoolTreeLeaf.False)) {
		listOf(BoolTreeLeaf.False)
	} else if (this.any { it is DidStateReference }) {
		this.filter { it == BoolTreeLeaf.True }
	} else {
		this
	}

fun BoolTreeAnd<BoolTreeLeaf>.simplify() = BoolTreeAnd(this.and.simplify())

fun CifAclOr.simplify(): CifAclOr {
	return if (this.isTrue()) {
		return AlwaysTree
	} else {
		BoolTreeOr(this.or.map { it.simplify() })
	}
}

fun CifAclOr.allReferences(): Set<DidStateReference> =
	this.or
		.flatMap {
			it.and.filterIsInstance<DidStateReference>()
		}.toSet()

fun CifAclOr.addSubAclForReference(
	ref: DidStateReference,
	subAcl: CifAclOr,
): CifAclOr {
	TODO("Implement")
}
