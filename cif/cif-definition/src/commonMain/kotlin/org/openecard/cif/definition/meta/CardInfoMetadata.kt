package org.openecard.cif.definition.meta

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

// TODO: think about how to solve the translation (name) and if it is necessary at all
@Serializable
data class CardInfoMetadata(
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
