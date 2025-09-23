package org.openecard.addons.tr03124

import kotlinx.serialization.Serializable

/**
 * Status of the ecard.
 *
 * The AusweisApp 2 responds with the following plaintext (`text/plain`):
 * ```
 * Implementation-Title: AusweisApp2
 * Implementation-Vendor: flathub
 * Implementation-Version: 2.3.2
 * Name: AusweisApp2
 * Specification-Title: TR-03124-1
 * Specification-Vendor: Federal Office for Information Security
 * Specification-Version: 1.4
 * ```
 */
@Serializable
data class EcardStatus(
	val app: ProductEntry,
	val specs: List<ProductEntry>,
) {
	@Serializable
	data class ProductEntry(
		val name: String,
		val vendor: String?,
		val version: String,
	)
}
