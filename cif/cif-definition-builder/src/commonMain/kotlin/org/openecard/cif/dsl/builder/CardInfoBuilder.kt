package org.openecard.cif.dsl.builder

import org.openecard.cif.definition.CardInfoDefinition
import org.openecard.cif.definition.CifVerifier
import org.openecard.cif.definition.app.ApplicationDefinition
import org.openecard.cif.definition.meta.CardInfoMetadata
import org.openecard.cif.dsl.api.CardInfoMetadataScope
import org.openecard.cif.dsl.api.CardInfoScope
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifSetScope
import org.openecard.cif.dsl.api.application.ApplicationScope
import org.openecard.cif.dsl.builder.app.ApplicationBuilder

class CardInfoBuilder :
	CardInfoScope,
	Builder<CardInfoDefinition> {
	private lateinit var metadataDefinition: CardInfoMetadata
	private var applications: Set<ApplicationDefinition>? = null

	override fun metadata(content: @CifMarker (CardInfoMetadataScope.() -> Unit)) {
		val builder = CardInfoMetadataBuilder()
		content(builder)
		this.metadataDefinition = builder.build()
	}

	override fun applications(content: @CifMarker (CifSetScope<ApplicationScope>.() -> Unit)) {
		val builder = CifSetBuilder<ApplicationScope, ApplicationBuilder>(builder = { ApplicationBuilder() })
		content(builder)
		applications = builder.build().map { it.build() }.toSet()
	}

	override fun build(): CardInfoDefinition {
		val cif =
			CardInfoDefinition(
				metadata = metadataDefinition,
				applications = applications ?: setOf(),
			)

		// TODO: perform sanity checks such as existence of DIDs and Datasets
		CifVerifier(cif).verify()

		return cif
	}
}
