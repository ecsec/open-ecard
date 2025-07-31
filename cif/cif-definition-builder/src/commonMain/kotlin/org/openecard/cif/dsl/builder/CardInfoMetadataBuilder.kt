package org.openecard.cif.dsl.builder

import org.openecard.cif.definition.meta.CardInfoMetadata
import org.openecard.cif.definition.meta.CardInfoStatus
import org.openecard.cif.dsl.api.CardInfoMetadataScope
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class CardInfoMetadataBuilder :
	CardInfoMetadataScope,
	Builder<CardInfoMetadata> {
	private var _id: String? = null
	override var id: String
		get() = requireNotNull(_id)
		set(value) {
			_id = value
		}
	private var _name: String? = null
	override var name: String
		get() = requireNotNull(_name)
		set(value) {
			_name = value
		}
	override var version: String? = null

	@OptIn(ExperimentalTime::class)
	override var creationDate: Instant? = null

	@OptIn(ExperimentalTime::class)
	override var modificationDate: Instant? = null
	override var status: CardInfoStatus? = null
	override var cardIssuer: String? = null

	@OptIn(ExperimentalTime::class)
	override fun build(): CardInfoMetadata =
		CardInfoMetadata(
			name = name,
			cardIssuer = cardIssuer,
			id = id,
			status = status,
			version = version,
			creationDate = creationDate,
			modificationDate = modificationDate,
		)
}
