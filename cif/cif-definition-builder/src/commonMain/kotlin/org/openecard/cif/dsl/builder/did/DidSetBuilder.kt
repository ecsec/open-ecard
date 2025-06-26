package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.DidDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.did.DidDslScope
import org.openecard.cif.dsl.api.did.DidSetScope
import org.openecard.cif.dsl.builder.Builder

class DidSetBuilder(
	val dids: MutableSet<DidDefinition> = mutableSetOf(),
) : DidSetScope,
	Builder<Set<DidDefinition>> {
	override fun pace(content: @CifMarker (DidDslScope.Pace.() -> Unit)) {
		val builder = PaceDidBuilder()
		content(builder)
		dids.add(builder.build())
	}

	override fun build(): Set<DidDefinition> = dids
}
