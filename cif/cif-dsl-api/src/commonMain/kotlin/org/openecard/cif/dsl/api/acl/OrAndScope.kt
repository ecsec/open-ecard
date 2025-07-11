package org.openecard.cif.dsl.api.acl

import org.openecard.cif.definition.acl.AclQualifier
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.acl.PaceAclQualifier

@AclTreeMarker
object OrAndScope {
	val Always: OrAndScopeResult.LeafResult
		get() = OrAndScopeResult.LeafResult.Always

	fun paceQualifier(hex: String): PaceAclQualifier = AclBoolTreeBuilder.paceQualifier(hex)

	@AclTreeMarker
	fun didState(
		name: String,
		active: Boolean,
		qualifier: AclQualifier? = null,
	): OrAndScopeResult.LeafResult =
		OrAndScopeResult.LeafResult(
			DidStateReference(
				name = name,
				active = active,
				stateQualifier = qualifier,
			),
		)

	@AclTreeMarker
	fun and(
		first: OrAndScopeResult.LeafResult,
		vararg many: OrAndScopeResult.LeafResult,
	): OrAndScopeResult.AndOperatorResult {
		val result = mutableListOf(first.leaf)
		result.addAll(many.map { it.leaf })
		return OrAndScopeResult.AndOperatorResult(
			result,
		)
	}

	@AclTreeMarker
	fun activeDidState(
		name: String,
		qualifier: AclQualifier? = null,
	): OrAndScopeResult.LeafResult = didState(name = name, active = true, qualifier = qualifier)

	@AclTreeMarker
	fun inActiveDidState(
		name: String,
		qualifier: AclQualifier? = null,
	): OrAndScopeResult.LeafResult = didState(name = name, active = false, qualifier = qualifier)
}

sealed interface OrAndScopeResult {
	data class LeafResult(
		val leaf: BoolTreeLeaf,
	) : OrAndScopeResult {
		companion object {
			val Always = LeafResult(BoolTreeLeaf.True)
		}
	}

	data class AndOperatorResult(
		val leaves: List<BoolTreeLeaf>,
	) : OrAndScopeResult
}
