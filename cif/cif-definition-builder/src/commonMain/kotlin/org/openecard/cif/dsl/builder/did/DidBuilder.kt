package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.DidDefinition
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.dsl.api.did.DidDslScope
import org.openecard.cif.dsl.builder.Builder

class DidBuilder :
	DidDslScope,
	Builder<DidDefinition> {
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

	override fun build(): DidDefinition {
		TODO("Support the various subclasses of DidDefinition")
	}
}
