package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.acl.AclDefinition

@Serializable
class PinDidDefinition(
	override val name: String,
	override val scope: DidScope,
	val authAcl: AclDefinition,
	val modifyAcl: AclDefinition,
	val parameters: PinDidParameters,
) : DidDefinition

@Serializable
data class PinDidParameters(
	val pwdFlags: Set<PasswordFlags>,
	val pwdType: PasswordType,
	val passwordRef: UByte,
	val minLength: Int,
	val maxLength: Int?,
	val storedLength: Int?,
	val padChar: UByte?,
)

enum class PasswordType {
	BCD,
	ISO_9564_1,
	ASCII_NUMERIC,
	UTF_8,
	HALF_NIBBLE_BCD,
}

enum class PasswordFlags {
	/**
	 * meaning that a user-given password shall not be converted to all-uppercase before presented to the card
	 */
	CASE_SENSITIVE,

	/**
	 * meaning that it is not possible to change the password
	 */
	CHANGE_DISABLED,

	/**
	 * meaning that it is not possible to unblock the password
	 */
	UNBLOCK_DISABLED,

	/**
	 * meaning that, depending on the length of the given password and the stored length, the password may need to be
	 * padded before being presented to the card
	 */
	NEEDS_PADDING,

	/**
	 * is an unblockingPassword (ISO/IEC 7816-4 resetting code), meaning that this password may be used for unblocking
	 * purposes, i.e. to reset the retry counter of the related authentication object to its initial value
	 */
	UNBLOCKING_PASSWORD,

	/**
	 * is a soPassword , meaning that the password is a Security Officer (administrator) password
	 */
	SO_PASSWORD,

	/**
	 * is disable-allowed , meaning that the password might be disabled
	 */
	DISABLE_ALLOWED,

	/**
	 * can be changed by just presenting new reference data to the card or if both old and new reference data needs to
	 * be presented.
	 * If the bit is set, both old and new reference data shall be presented;
	 * otherwise only new reference data needs to be presented (exchangeRefData)
	 */
	EXCHANGE_REF_DATA,
}
