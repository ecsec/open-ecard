package org.openecard.cif.dsl.api

import org.openecard.cif.dsl.api.application.ApplicationScope
import org.openecard.cif.dsl.api.capabilities.CardCapabilitiesScope

interface CardInfoScope : CifScope {
	fun metadata(content: @CifMarker CardInfoMetadataScope.() -> Unit)

	fun capabilities(content: @CifMarker CardCapabilitiesScope.() -> Unit)

	fun applications(content: @CifMarker CifSetScope<ApplicationScope>.() -> Unit)
}
