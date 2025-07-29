package org.openecard.cif.dsl.api

import kotlinx.datetime.Instant
import org.openecard.cif.definition.meta.CardInfoStatus

interface CardInfoMetadataScope : CifScope {
	/**
	 * Unique identifier of this CIF.
	 */
	var id: String

	/**
	 * Human-readable name identifying the card type.
	 */
	var name: String

	/**
	 * Semantic version string of this CIF.
	 */
	var version: String?
	var creationDate: Instant?
	var modificationDate: Instant?
	var status: CardInfoStatus?
	var cardIssuer: String?
}
