package org.openecard.cif.dsl.api

import org.openecard.cif.definition.meta.CardInfoStatus
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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

	@OptIn(ExperimentalTime::class)
	var creationDate: Instant?

	@OptIn(ExperimentalTime::class)
	var modificationDate: Instant?
	var status: CardInfoStatus?
	var cardIssuer: String?
}
