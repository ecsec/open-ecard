package org.openecard.cif.dsl.api

import org.openecard.cif.dsl.api.application.ApplicationScope

interface CardInfoScope : CifScope {
	fun metadata(content: @CifMarker CardInfoMetadataScope.() -> Unit)

	fun applications(content: @CifMarker CifSetScope<ApplicationScope>.() -> Unit)
}
