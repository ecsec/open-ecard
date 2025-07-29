package org.openecard.cif.dsl.api.acl.extension

import org.openecard.cif.definition.acl.AclQualifier
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.dsl.api.acl.AclBoolTreeBuilder
import org.openecard.cif.dsl.api.acl.AclTreeMarker
import org.openecard.cif.dsl.api.acl.AndOrScope
import org.openecard.cif.dsl.api.acl.AndOrScopeResult
import org.openecard.cif.dsl.api.acl.OrAndScope
import org.openecard.cif.dsl.api.acl.OrAndScopeResult

@AclTreeMarker
sealed interface DidStateScope {
	var name: String
	var active: Boolean
	var stateQualifier: AclQualifier?
}

@AclTreeMarker
class DidStateScopeBuilder : DidStateScope {
	private var _name: String? = null
	override var name: String
		get() = _name!!
		set(value) {
			_name = value
		}
	private var _active: Boolean? = null
	override var active: Boolean
		get() = _active!!
		set(value) {
			_active = value
		}
	override var stateQualifier: AclQualifier? = null

	fun build(): DidStateReference =
		DidStateReference(
			name = name,
			active = active,
			stateQualifier = stateQualifier,
		)
}

fun AclBoolTreeBuilder.didState(
	@AclTreeMarker content: DidStateScope.() -> Unit,
): CifAclOr {
	val builder = DidStateScopeBuilder()
	content(builder)
	val built = builder.build()
	return didState(name = built.name, active = built.active, qualifier = built.stateQualifier)
}

fun AndOrScope.didState(
	@AclTreeMarker content: DidStateScope.() -> Unit,
): AndOrScopeResult.LeafResult {
	val builder = DidStateScopeBuilder()
	content(builder)
	val built = builder.build()
	return didState(name = built.name, active = built.active, qualifier = built.stateQualifier)
}

fun AndOrScope.activeDidState(
	@AclTreeMarker content: DidStateScope.() -> Unit,
): AndOrScopeResult.LeafResult {
	val builder = DidStateScopeBuilder()
	builder.active = true
	content(builder)
	val built = builder.build()
	return didState(name = built.name, active = built.active, qualifier = built.stateQualifier)
}

fun OrAndScope.didState(
	@AclTreeMarker content: DidStateScope.() -> Unit,
): OrAndScopeResult.LeafResult {
	val builder = DidStateScopeBuilder()
	content(builder)
	val built = builder.build()
	return didState(name = built.name, active = built.active, qualifier = built.stateQualifier)
}

fun OrAndScope.activeDidState(
	@AclTreeMarker content: DidStateScope.() -> Unit,
): OrAndScopeResult.LeafResult {
	val builder = DidStateScopeBuilder()
	builder.active = true
	content(builder)
	val built = builder.build()
	return didState(name = built.name, active = built.active, qualifier = built.stateQualifier)
}
