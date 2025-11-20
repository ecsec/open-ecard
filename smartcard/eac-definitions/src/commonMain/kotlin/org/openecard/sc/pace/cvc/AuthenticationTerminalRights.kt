package org.openecard.sc.pace.cvc

import org.openecard.utils.common.BitSet
import kotlin.enums.EnumEntries

interface BitAssociation {
	val bitNumber: Int
}

class RightsBitSet<T>(
	private val bits: BitSet,
	private val entries: EnumEntries<T>,
) where T : BitAssociation, T : Enum<T> {
	operator fun get(flag: T): Boolean = bits[flag.bitNumber]

	operator fun set(
		flag: T,
		value: Boolean,
	) {
		bits[flag.bitNumber] = value
	}

	fun clear() {
		entries.forEach { bits[it.bitNumber] = false }
	}

	fun toMap(): Map<T, Boolean> = entries.associateWith { bits[it.bitNumber] }

	override fun toString(): String =
		toMap()
			.map {
				"${it.key.name}=${it.value}"
			}.joinToString(prefix = "RightsBits<", postfix = ">")
}

/**
 * Represents the write access data groups.
 * See BSI-TR-03110-4, section 2.2.3.2.
 */
enum class WriteAccess(
	override val bitNumber: Int,
) : BitAssociation {
	DG17(37),
	DG18(36),
	DG19(35),
	DG20(34),
	DG21(33),
	DG22(32),
}

/**
 * Represents the read access data groups.
 * See BSI-TR-03110-4, section 2.2.3.2.
 */
enum class ReadAccess(
	override val bitNumber: Int,
) : BitAssociation {
	DG01(8),
	DG02(9),
	DG03(10),
	DG04(11),
	DG05(12),
	DG06(13),
	DG07(14),
	DG08(15),
	DG09(16),
	DG10(17),
	DG11(18),
	DG12(19),
	DG13(20),
	DG14(21),
	DG15(22),
	DG16(23),
	DG17(24),
	DG18(25),
	DG19(26),
	DG20(27),
	DG21(28),
	DG22(29),
}

/**
 * Represents the special functions.
 * See BSI-TR-03110-4, section 2.2.3.2.
 */
enum class SpecialFunction(
	override val bitNumber: Int,
) : BitAssociation {
	PSEUDONYMOUS_SIGNATURE_AUTHENTICATION(30),
	INSTALL_QUALIFIED_CERTIFICATE(7),
	INSTALL_CERTIFICATE(6),
	PIN_MANAGEMENT(5),
	CAN_ALLOWED(4),
	PRIVILEGED_TERMINAL(3),
	RESTRICTED_IDENTIFICATION(2),
	COMMUNITY_ID_VERIFICATION(1),
	AGE_VERIFICATION(0),
}
