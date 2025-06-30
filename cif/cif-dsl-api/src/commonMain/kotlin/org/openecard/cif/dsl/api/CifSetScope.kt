package org.openecard.cif.dsl.api

interface CifSetScope<T : CifScope> : CifScope {
	fun add(content: @CifMarker T.() -> Unit)
}
