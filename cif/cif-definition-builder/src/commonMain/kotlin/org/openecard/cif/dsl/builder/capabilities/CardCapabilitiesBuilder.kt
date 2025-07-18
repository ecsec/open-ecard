package org.openecard.cif.dsl.builder.capabilities

import org.openecard.cif.definition.capabilities.CardCapabilitiesDefinition
import org.openecard.cif.definition.capabilities.CommandCodingDefinitions
import org.openecard.cif.definition.capabilities.DataCodingDefinitions
import org.openecard.cif.definition.capabilities.SelectionMethodsDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.capabilities.CardCapabilitiesScope
import org.openecard.cif.dsl.api.capabilities.CommandCodingScope
import org.openecard.cif.dsl.api.capabilities.DataCodingScope
import org.openecard.cif.dsl.api.capabilities.SelectionMethodsScope
import org.openecard.cif.dsl.builder.Builder

class CardCapabilitiesBuilder :
	CardCapabilitiesScope,
	Builder<CardCapabilitiesDefinition> {
	private var _selectionMethods: SelectionMethodsDefinition? = null
	var selectionMethods: SelectionMethodsDefinition
		get() = checkNotNull(_selectionMethods)
		set(value) {
			_selectionMethods = value
		}
	var dataCoding: DataCodingDefinitions? = null
	var commandCoding: CommandCodingDefinitions? = null

	override fun selectionMethods(content: @CifMarker (SelectionMethodsScope.() -> Unit)) {
		val builder = SelectionMethodsBuilder()
		content(builder)
		this.selectionMethods = builder.build()
	}

	override fun dataCoding(content: @CifMarker (DataCodingScope.() -> Unit)) {
		val builder = DataCodingBuilder()
		content(builder)
		this.dataCoding = builder.build()
	}

	override fun commandCoding(content: @CifMarker (CommandCodingScope.() -> Unit)) {
		val builder = CommandCodingBuilder()
		content(builder)
		this.commandCoding = builder.build()
	}

	override fun build(): CardCapabilitiesDefinition =
		CardCapabilitiesDefinition(
			selectionMethods = selectionMethods,
			dataCoding = dataCoding,
			commandCoding = commandCoding,
		)
}
