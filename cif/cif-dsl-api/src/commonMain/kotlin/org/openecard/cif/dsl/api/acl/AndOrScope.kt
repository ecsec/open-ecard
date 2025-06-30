package org.openecard.cif.dsl.api.acl

import org.openecard.cif.definition.acl.AclQualifier
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.acl.PaceAclQualifier

@AclTreeMarker
object AndOrScope {
	val Always: AndOrScopeResult.LeafResult
		get() = AndOrScopeResult.LeafResult.Always
	val Never: AndOrScopeResult.LeafResult
		get() = AndOrScopeResult.LeafResult.Never

	fun paceQualifier(hex: String): PaceAclQualifier = AclBoolTreeBuilder.paceQualifier(hex)

	@AclTreeMarker
	fun didState(
		name: String,
		active: Boolean,
		qualifier: AclQualifier? = null,
	): AndOrScopeResult.LeafResult =
		AndOrScopeResult.LeafResult(
			DidStateReference(
				name = name,
				active = active,
				stateQualifier = qualifier,
			),
		)

	@AclTreeMarker
	fun or(
		first: AndOrScopeResult.LeafResult,
		second: AndOrScopeResult.LeafResult,
		vararg many: AndOrScopeResult.LeafResult,
	): AndOrScopeResult.OrOperatorResult {
		val result = mutableListOf(first.leaf, second.leaf)
		result.addAll(many.map { it.leaf })
		return AndOrScopeResult.OrOperatorResult(
			result,
		)
	}

	@AclTreeMarker
	fun activeDidState(
		name: String,
		qualifier: AclQualifier? = null,
	): AndOrScopeResult.LeafResult = didState(name = name, active = true, qualifier = qualifier)

	@AclTreeMarker
	fun inActiveDidState(
		name: String,
		qualifier: AclQualifier? = null,
	): AndOrScopeResult.LeafResult = didState(name = name, active = false, qualifier = qualifier)
}

interface AndOrScopeResult {
	data class LeafResult(
		val leaf: BoolTreeLeaf,
	) : AndOrScopeResult {
		companion object {
			val Always = LeafResult(BoolTreeLeaf.True)
			val Never = LeafResult(BoolTreeLeaf.False)
		}
	}

	data class OrOperatorResult(
		val leaves: List<BoolTreeLeaf>,
	) : AndOrScopeResult
}
