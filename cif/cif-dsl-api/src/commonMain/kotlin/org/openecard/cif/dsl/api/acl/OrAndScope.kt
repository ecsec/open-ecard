package org.openecard.cif.dsl.api.acl

import org.openecard.cif.definition.acl.AclQualifier
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.acl.PaceAclQualifier

@AslTreeMarker
object OrAndScope {
	val Always: OrAndScopeResult.LeafResult
		get() = OrAndScopeResult.LeafResult.Always
	val Never: OrAndScopeResult.LeafResult
		get() = OrAndScopeResult.LeafResult.Never

	fun paceQualifier(hex: String): PaceAclQualifier = AclBoolTreeBuilder.paceQualifier(hex)

	@AslTreeMarker
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

	@AslTreeMarker
	fun and(
		first: OrAndScopeResult.LeafResult,
		second: OrAndScopeResult.LeafResult,
		vararg many: OrAndScopeResult.LeafResult,
	): OrAndScopeResult.AndOperatorResult {
		val result = mutableListOf(first.leaf, second.leaf)
		result.addAll(many.map { it.leaf })
		return OrAndScopeResult.AndOperatorResult(
			result,
		)
	}

	@AslTreeMarker
	fun activeDidState(
		name: String,
		qualifier: AclQualifier? = null,
	): OrAndScopeResult.LeafResult = didState(name = name, active = true, qualifier = qualifier)

	@AslTreeMarker
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
			val Never = LeafResult(BoolTreeLeaf.False)
		}
	}

	data class AndOperatorResult(
		val leaves: List<BoolTreeLeaf>,
	) : OrAndScopeResult
}
