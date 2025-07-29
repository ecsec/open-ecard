package org.openecard.cif.dsl.builder

import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope
import org.openecard.cif.dsl.api.CifSetScope

class CifSetBuilder<T : CifScope, B : T>(
	val builder: () -> B,
	val builders: MutableSet<B> = mutableSetOf(),
) : CifSetScope<T>,
	Builder<Set<B>> {
	override fun add(content: @CifMarker (T.() -> Unit)) {
		val builder = builder()
		content(builder)
		builders.add(builder)
	}

	override fun build(): Set<B> = builders
}
