package org.openecard.cif.definition.cardcall

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class TemplateApduCallDefinition(
	val header: ApduTemplateValue,
	val data: ApduTemplateValue?,
	val expectedLength: ULong?,
)

private val templatePattern =
	"""
	([0-9a-fA-F]{2}|(\{[a-zA-Z][a-zA-Z0-9]*(\s+(([a-zA-Z][a-zA-Z0-9]*)|(0x([0-9a-fA-F]{2})+)))*\}))+
	""".trimIndent().toRegex()

/**
 * A ApduTemplateValue consists of a hexBinary value or a template expression intermixed in arbitrary multiplicity.
 *
 * The template expression is a simplified S-Expression initiated and terminated by curly braces (`{` and `}`).
 * The first symbol must be a template symbol which is defined as `[a-zA-Z][a-zA-Z0-9]*`.
 * The following elements are separated by any whitespace.
 * For the following symbols either a template symbol or a hex string starting with `0x` can be used.
 *
 * The evaluation sematic of the symbols is as follows. In the single element form, the S-Expression is evaluated
 * depending on its type in the context. That means it is either a function, or a value fitting in the hexBinary value
 * of the whole value. If multiple elements are inside the S-Expression, the first element MUST denote a function while
 * the remaining ones MUST be symbolic values.
 */
@Serializable
@JvmInline
value class ApduTemplateValue(
	val template: String,
) {
	init {
		if (!templatePattern.matches(template)) {
			throw IllegalArgumentException("The given template is not valid according to the template definition.")
		}
	}
}
