package org.openecard.cif.definition.meta

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class CardInfoMetadata
	@OptIn(ExperimentalTime::class)
	constructor(
		/**
		 * Unique identifier of this CIF.
		 */
		val id: String,
		/**
		 * Human-readable name identifying the card type.
		 */
		val name: String,
		/**
		 * Semantic version string of this CIF.
		 */
		val version: String?,
		val creationDate: Instant?,
		val modificationDate: Instant?,
		val status: CardInfoStatus?,
		val cardIssuer: String?,
	)
