package org.openecard.richclient

import dev.icerock.moko.resources.StringResource
import java.util.Enumeration
import java.util.ResourceBundle

/**
 * Wrapper for Moko resources in a Java ResourceBundle.
 * This class always returns values for the configured default locale.
 */
class MokoResourceBundle(
	strings: List<StringResource>,
) : ResourceBundle() {
	private val entries = strings.associateBy { it.key }

	override fun handleGetObject(key: String): Any? = entries[key]?.localized()

	override fun getKeys(): Enumeration<String> {
		val it = entries.keys.iterator()
		return object : Enumeration<String> {
			override fun hasMoreElements(): Boolean = it.hasNext()

			override fun nextElement(): String = it.next()
		}
	}
}
