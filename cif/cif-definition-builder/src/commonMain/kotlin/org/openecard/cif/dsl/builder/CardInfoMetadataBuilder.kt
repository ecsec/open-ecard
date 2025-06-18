package org.openecard.cif.dsl.builder

import kotlinx.datetime.Instant
import org.openecard.cif.definition.meta.CardInfoMetadata
import org.openecard.cif.definition.meta.CardInfoStatus
import org.openecard.cif.dsl.api.CardInfoMetadataScope

class CardInfoMetadataBuilder :
	CardInfoMetadataScope,
	Builder<CardInfoMetadata> {
	private var _id: String? = null
	override var id: String
		get() = _id!!
		set(value) {
			_id = value
		}
	private var _name: String? = null
	override var name: String
		get() = _name!!
		set(value) {
			_name = value
		}
	override var version: String? = null
	override var creationDate: Instant? = null
	override var modificationDate: Instant? = null
	override var status: CardInfoStatus? = null
	override var cardIssuer: String? = null

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
