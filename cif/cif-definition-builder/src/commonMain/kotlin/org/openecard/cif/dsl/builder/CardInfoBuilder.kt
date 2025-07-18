package org.openecard.cif.dsl.builder

import org.openecard.cif.definition.CardInfoDefinition
import org.openecard.cif.definition.app.ApplicationDefinition
import org.openecard.cif.definition.capabilities.CardCapabilitiesDefinition
import org.openecard.cif.definition.meta.CardInfoMetadata
import org.openecard.cif.dsl.api.CardInfoMetadataScope
import org.openecard.cif.dsl.api.CardInfoScope
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifSetScope
import org.openecard.cif.dsl.api.application.ApplicationScope
import org.openecard.cif.dsl.api.capabilities.CardCapabilitiesScope
import org.openecard.cif.dsl.builder.app.ApplicationBuilder
import org.openecard.cif.dsl.builder.capabilities.CardCapabilitiesBuilder

class CardInfoBuilder :
	CardInfoScope,
	Builder<CardInfoDefinition> {
	private var metadataDefinition: CardInfoMetadata? = null
	private var capabilitiesDefinition: CardCapabilitiesDefinition? = null
	private var applications: Set<ApplicationDefinition>? = null

	override fun metadata(content: @CifMarker (CardInfoMetadataScope.() -> Unit)) {
		val builder = CardInfoMetadataBuilder()
		content(builder)
		this.metadataDefinition = builder.build()
	}

	override fun capabilities(content: @CifMarker (CardCapabilitiesScope.() -> Unit)) {
		val builder = CardCapabilitiesBuilder()
		content(builder)
		this.capabilitiesDefinition = builder.build()
	}

	override fun applications(content: @CifMarker (CifSetScope<ApplicationScope>.() -> Unit)) {
		val builder = CifSetBuilder<ApplicationScope, ApplicationBuilder>(builder = { ApplicationBuilder() })
		content(builder)
		applications = builder.build().map { it.build() }.toSet()
	}

	override fun build(): CardInfoDefinition {
		val cif =
			CardInfoDefinition(
				metadata = checkNotNull(metadataDefinition),
				capabilities = capabilitiesDefinition,
				applications = applications ?: setOf(),
			)

		return cif
	}
}
