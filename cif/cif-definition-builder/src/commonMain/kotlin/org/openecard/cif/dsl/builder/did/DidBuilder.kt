package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.DidDefinition
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.dsl.api.did.DidDslScope
import org.openecard.cif.dsl.builder.Builder

abstract class DidBuilder<T : DidDefinition> :
	DidDslScope,
	Builder<T> {
	private var _name: String? = null
	override var name: String
		get() = requireNotNull(_name)
		set(value) {
			_name = value
		}
	private var _scope: DidScope? = null
	override var scope: DidScope
		get() = requireNotNull(_scope)
		set(value) {
			_scope = value
		}
}
