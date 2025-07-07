package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.InterIndustryClassByte
import org.openecard.sc.tlv.Tlv
import org.openecard.utils.common.bitSetOf
import org.openecard.utils.common.mergeToArray
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

data class Mse(
	val flags: P1Flags,
	val command: Command,
	val p2: UByte,
	val data: PrintableUByteArray?,
	val cla: InterIndustryClassByte = InterIndustryClassByte.Default,
) : IsoCommandApdu,
	SecurityCommandApdu {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val apdu: CommandApdu by lazy {
		val p1 = flags.code or command.code
		CommandApdu(cla.byte, 0x22u, p1, p2, (data ?: ubyteArrayOf().toPrintable()))
	}

	fun setCommandChaining(isChaining: Boolean = true) = copy(cla = cla.setCommandChaining(isChaining))

	enum class Command(
		val code: UByte,
	) {
		SET(1u),
		STORE(2u),
		RESTORE(3u),
		ERASE(4u),
	}

	enum class Tag(
		val code: UByte,
	) {
		/**
		 * Control reference template for authentication (AT)
		 */
		AT(0xA4u),

		/**
		 * Control reference template for key agreement (KAT)
		 */
		KAT(0xA6u),

		/**
		 * Control reference template for hash-code (HT)
		 */
		HT(0xAAu),

		/**
		 * Control reference template for cryptographic checksum (CCT)
		 */
		CCT(0xB4u),

		/**
		 * Control reference template for digital signature (DST)
		 */
		DST(0xB6u),

		/**
		 * Control reference template for confidentiality (CT)
		 */
		CT(0xB8u),
	}

	data class P1Flags(
		val smInCommand: Boolean,
		val smInResponse: Boolean,
		val computationDecipherIntAuthKeyAgree: Boolean,
		val verifyEncipherExtAuthKeyAgree: Boolean,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		val code by lazy {
			bitSetOf(0u)
				.apply {
					set(4, smInCommand)
					set(5, smInResponse)
					set(6, computationDecipherIntAuthKeyAgree)
					set(7, verifyEncipherExtAuthKeyAgree)
				}.toUByteArray()[0]
		}

		fun setSmInCommand(value: Boolean): P1Flags = copy(smInCommand = value)

		fun setSmInResponse(value: Boolean): P1Flags = copy(smInResponse = value)

		fun setComputationDecipherIntAuthKeyAgree(value: Boolean): P1Flags = copy(computationDecipherIntAuthKeyAgree = value)

		fun setVerifyEncipherExtAuthKeyAgree(value: Boolean): P1Flags = copy(verifyEncipherExtAuthKeyAgree = value)
	}

	companion object {
		val p1FlagsAllUnset =
			P1Flags(
				smInCommand = false,
				smInResponse = false,
				computationDecipherIntAuthKeyAgree = false,
				verifyEncipherExtAuthKeyAgree = false,
			)
		val p1FlagsAllSet =
			P1Flags(
				smInCommand = true,
				smInResponse = true,
				computationDecipherIntAuthKeyAgree = true,
				verifyEncipherExtAuthKeyAgree = true,
			)

		fun mseStore(seid: UByte): Mse = Mse(p1FlagsAllSet, Command.STORE, seid, null)

		fun mseRestore(seid: UByte): Mse = Mse(p1FlagsAllSet, Command.RESTORE, seid, null)

		fun mseErase(seid: UByte): Mse = Mse(p1FlagsAllSet, Command.ERASE, seid, null)

		@OptIn(ExperimentalUnsignedTypes::class)
		fun mseSet(
			flags: P1Flags,
			tag: Tag,
			data: UByteArray,
		): Mse = Mse(flags, Command.SET, tag.code, data.toPrintable())

		@OptIn(ExperimentalUnsignedTypes::class)
		fun mseSet(
			flags: P1Flags,
			tag: Tag,
			data: List<Tlv>,
		): Mse = mseSet(flags, tag, data.map { it.toBer() }.mergeToArray())
	}
}
