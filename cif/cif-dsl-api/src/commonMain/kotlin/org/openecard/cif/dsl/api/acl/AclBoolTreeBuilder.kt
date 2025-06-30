package org.openecard.cif.dsl.api.acl

import org.openecard.cif.definition.acl.AclQualifier
import org.openecard.cif.definition.acl.AlwaysTree
import org.openecard.cif.definition.acl.BoolTreeAnd
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.acl.NeverTree
import org.openecard.cif.definition.acl.PaceAclQualifier
import org.openecard.utils.serialization.toPrintable

@AclTreeMarker
object AclBoolTreeBuilder {
	val Never = NeverTree

	val Always = AlwaysTree

	@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
	fun paceQualifier(hex: String): PaceAclQualifier = PaceAclQualifier(hex.hexToUByteArray().toPrintable())

	@AclTreeMarker
	fun didState(
		name: String,
		active: Boolean,
		qualifier: AclQualifier? = null,
	): CifAclOr =
		CifAclOr(
			listOf(
				BoolTreeAnd(
					listOf(
						DidStateReference(
							name = name,
							active = active,
							stateQualifier = qualifier,
						),
					),
				),
			),
		)

	fun activeDidState(
		name: String,
		qualifier: AclQualifier? = null,
	) = didState(name = name, active = true, qualifier = qualifier)

	fun inactiveDidState(
		name: String,
		qualifier: AclQualifier? = null,
	) = didState(name = name, active = false, qualifier = qualifier)

	fun and(
		@AclTreeMarker first: (AndOrScope.() -> AndOrScopeResult),
		@AclTreeMarker vararg content: (AndOrScope.() -> AndOrScopeResult),
	): CifAclOr {
		val candidates =
			mutableListOf(
				first(AndOrScope),
			)
		candidates.addAll(content.map { it(AndOrScope) })
		val leaves = mutableListOf<BoolTreeLeaf>()
		val orStatements = mutableListOf<List<BoolTreeLeaf>>()

		for (candidate in candidates) {
			when (candidate) {
				is AndOrScopeResult.LeafResult -> leaves.add(candidate.leaf)
				is AndOrScopeResult.OrOperatorResult -> orStatements.add(candidate.leaves)
			}
		}

		val results = mutableListOf<BoolTreeAnd<BoolTreeLeaf>>()

		for (orStatement in orStatements) {
			orStatement.forEach { literal ->
				results.add(
					BoolTreeAnd(
						leaves + literal,
					),
				)
			}
		}

		return CifAclOr(results)
	}

	fun or(
		@AclTreeMarker first: (OrAndScope.() -> OrAndScopeResult),
		@AclTreeMarker vararg content: (OrAndScope.() -> OrAndScopeResult),
	): CifAclOr {
		val candidates =
			mutableListOf(
				first(OrAndScope),
			)
		candidates.addAll(content.map { it(OrAndScope) })
		val results = mutableListOf<BoolTreeAnd<BoolTreeLeaf>>()

		for (candidate in candidates) {
			when (candidate) {
				is OrAndScopeResult.LeafResult -> results.add(BoolTreeAnd(listOf(candidate.leaf)))
				is OrAndScopeResult.AndOperatorResult -> results.add(BoolTreeAnd(candidate.leaves))
			}
		}

		return CifAclOr(results)
	}

	fun anyOf(
		@AclTreeMarker first: (OrAndScope.() -> OrAndScopeResult),
		@AclTreeMarker vararg content: (OrAndScope.() -> OrAndScopeResult),
	): CifAclOr = or(first, *content)
}
