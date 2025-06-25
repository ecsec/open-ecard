package org.openecard.cif.dsl.builder.app

import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.app.ApplicationDefinition
import org.openecard.cif.definition.dataset.DataSetDefinition
import org.openecard.cif.definition.did.DidDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifSetScope
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.application.ApplicationScope
import org.openecard.cif.dsl.api.dataset.DataSetScope
import org.openecard.cif.dsl.api.did.DidDslScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.cif.dsl.builder.CifSetBuilder
import org.openecard.cif.dsl.builder.acl.AclBuilder
import org.openecard.cif.dsl.builder.dataset.DataSetBuilder
import org.openecard.cif.dsl.builder.did.DidBuilder
import org.openecard.utils.serialization.PrintableUByteArray

class ApplicationBuilder :
	ApplicationScope,
	Builder<ApplicationDefinition> {
	private var _aid: PrintableUByteArray? = null
	override var aid: PrintableUByteArray
		get() = requireNotNull(_aid)
		set(value) {
			_aid = value
		}
	private var _name: String? = null
	override var name: String
		get() = requireNotNull(_name)
		set(value) {
			_name = value
		}
	override var description: String? = null
	var selectAcl: AclDefinition? = null
	private var dids: Set<DidDefinition>? = null
	var dataSets: Set<DataSetDefinition>? = null

	override fun selectAcl(content: @CifMarker (AclScope.() -> Unit)) {
		val builder = AclBuilder()
		content(builder)
		this.selectAcl = builder.build()
	}

	override fun dids(content: @CifMarker (CifSetScope<DidDslScope>.() -> Unit)) {
		val builder = CifSetBuilder<DidDslScope, DidBuilder>(builder = { DidBuilder() })
		content(builder)
		dids = builder.build().map { it.build() }.toSet()
	}

	override fun dataSets(content: @CifMarker (CifSetScope<DataSetScope>.() -> Unit)) {
		val builder = CifSetBuilder<DataSetScope, DataSetBuilder>(builder = { DataSetBuilder() })
		content(builder)
		dataSets = builder.build().map { it.build() }.toSet()
	}

	override fun build(): ApplicationDefinition =
		ApplicationDefinition(
			name = name,
			aid = aid,
			description = description,
			selectAcl = selectAcl ?: AclDefinition(mapOf()),
			dids = dids ?: setOf(),
			dataSets = dataSets ?: setOf(),
		)
}
