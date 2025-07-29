package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.acl.AclDefinition

@Serializable
class PinDidDefinition(
	override val name: String,
	override val scope: DidScope,
	val authAcl: AclDefinition,
	val modifyAcl: AclDefinition,
	val resetAcl: AclDefinition,
	val parameters: PinDidParameters,
) : DidDefinition

@Serializable
data class PinDidParameters(
	val pwdFlags: Set<PasswordFlags>,
	override val pwdType: PasswordType,
	val passwordRef: UByte,
	override val minLength: UInt,
	override val maxLength: UInt?,
	override val storedLength: UInt?,
	override val padChar: UByte?,
	val unblockingParameters: UnblockingParameters?,
) : PasswordEncodingDefinition

@Serializable
data class UnblockingParameters(
	override val pwdType: PasswordType,
	override val minLength: UInt,
	override val maxLength: UInt?,
	override val storedLength: UInt?,
	override val padChar: UByte?,
) : PasswordEncodingDefinition

interface PasswordEncodingDefinition {
	val pwdType: PasswordType
	val minLength: UInt
	val maxLength: UInt?
	val storedLength: UInt?
	val padChar: UByte?
}

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

	// 	/*
	// 	 * is an unblockingPassword (ISO/IEC 7816-4 resetting code), meaning that this password may be used for unblocking
	// 	 * purposes, i.e. to reset the retry counter of the related authentication object to its initial value
	// 	 */
	// UNBLOCKING_PASSWORD,
	// this is the iso definition, but we use a different one as we don't create PUK DIDs, but rather say a DID may be
	// used together with a PUK

	/**
	 * is a soPassword , meaning that the password is a Security Officer (administrator) password
	 */
	SO_PASSWORD,

	/**
	 * is disable-allowed , meaning that the password might be disabled
	 */
	DISABLE_ALLOWED,

	/**
	 * Password modify works with the old password, in order to change it (P1=00)
	 */
	MODIFY_WITH_OLD_PASSWORD,

	/**
	 * Password modify works without the old password, in order to change it (P1=01)
	 */
	MODIFY_WITHOUT_OLD_PASSWORD,

	/**
	 * Password reset works without reference data (P1=03)
	 */
	RESET_RETRY_COUNTER_WITHOUT_DATA,

	/**
	 * Password reset works with unblocking and reference data (P1=00)
	 */
	RESET_RETRY_COUNTER_WITH_UNBLOCK_AND_PASSWORD,

	/**
	 * Password reset works with reference data (P1=02)
	 */
	RESET_RETRY_COUNTER_WITH_PASSWORD,

	/**
	 * Password reset works with unblocking data (P1=01)
	 */
	RESET_RETRY_COUNTER_WITH_UNBLOCK,
}
