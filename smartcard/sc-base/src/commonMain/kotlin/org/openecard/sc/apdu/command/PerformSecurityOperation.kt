package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.InterIndustryClassByte
import org.openecard.sc.tlv.Tlv
import org.openecard.utils.common.mergeToArray
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

class PerformSecurityOperation(
	val p1: UByte,
	val p2: UByte,
	val data: PrintableUByteArray?,
	val le: UShort? = null,
	val forceExtendedLength: Boolean = false,
	val cla: InterIndustryClassByte = InterIndustryClassByte.Default,
) : IsoCommandApdu,
	SecurityCommandApdu {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val apdu: CommandApdu by lazy {
		CommandApdu(
			cla.byte,
			0x2Au,
			p1,
			p2,
			(data ?: ubyteArrayOf().toPrintable()),
			le,
			forceExtendedLength,
		)
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun computeChecksum(checksumInput: UByteArray): PerformSecurityOperation =
			PerformSecurityOperation(0x8Eu, 0x80u, checksumInput.toPrintable(), le = 0u)

		/**
		 * DOs relevant to the VERIFY CRYPTOGRAPHIC CHECKSUM operation (e.g. DO ‘80’, ‘8E’ see Table 2)
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyChecksum(checksumDos: List<Tlv>) =
			PerformSecurityOperation(0x00u, 0xA2u, checksumDos.map { it.toBer() }.mergeToArray().toPrintable())

		/**
		 * If P2 = '9A': data to be signed or integrated in the signature process
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun signData(signatureInput: UByteArray): PerformSecurityOperation =
			PerformSecurityOperation(0x9Eu, 0x9Au, signatureInput.toPrintable(), le = 0u)

		/**
		 * If P2 = 'AC': DOs relevant for DSI (the value field of these DOs are signed or integrated in the signature process)
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun signDoValues(signatureDos: List<Tlv>): PerformSecurityOperation =
			PerformSecurityOperation(0x9Eu, 0xACu, signatureDos.map { it.toBer() }.mergeToArray().toPrintable(), le = 0u)

		/**
		 * If P2 = 'BC': DOs relevant for DSI (the DOs are signed or integrated in the signature process)
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun signDos(signatureDos: List<Tlv>): PerformSecurityOperation =
			PerformSecurityOperation(0x9Eu, 0xBCu, signatureDos.map { it.toBer() }.mergeToArray().toPrintable(), le = 0u)

		/**
		 * DOs relevant to the VERIFY DIGITAL SIGNATURE operation (e.g. DO ‘9A’, ‘AC’ and ‘BC’, and DO ‘9E’, see Table 2)
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifySignature(signatureDos: List<Tlv>) =
			PerformSecurityOperation(0x00u, 0xA8u, signatureDos.map { it.toBer() }.mergeToArray().toPrintable())

		@OptIn(ExperimentalUnsignedTypes::class)
		fun hashData(hashData: UByteArray): PerformSecurityOperation =
			PerformSecurityOperation(0x90u, 0x80u, hashData.toPrintable(), le = 0u)

		/**
		 * If P2 = 'A0': DOs relevant for hashing (e.g. '90' for the intermediate hash code, '80' for the last text block)
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun hashDos(
			signatureDos: List<Tlv>,
			le: UShort? = 0u,
		): PerformSecurityOperation =
			PerformSecurityOperation(0x90u, 0xA0u, signatureDos.map { it.toBer() }.mergeToArray().toPrintable(), le = le)

		/**
		 * Tag 92: Certificate (non BER-TLV coded data)
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyCertificateDirect(certData: UByteArray): PerformSecurityOperation =
			PerformSecurityOperation(0x00u, 0x92u, certData.toPrintable())

		/**
		 * Tag AE: Input template for certificate verification (signed signature input consisting of non BER-TLV coded data)
		 *
		 * The certificate is not self-descriptive (P2 = 'AE'): the card retrieves a public key in the certificate
		 * either implicitly or explicitly by using the public key tag in a headerlist describing the content of
		 * the certificate.
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyCertificateNonBer(certData: List<Tlv>): PerformSecurityOperation =
			PerformSecurityOperation(0x00u, 0xAEu, certData.map { it.toBer() }.mergeToArray().toPrintable())

		/**
		 * Tag BE: Input template for certificate verification (signed signature input consisting of BER-TLV coded data)
		 *
		 * The certificate is self-descriptive (P2 = 'BE'): the card retrieves a public key identified by its tag in
		 * the (recovered) certificate content.
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyCertificateBer(certData: List<Tlv>): PerformSecurityOperation =
			PerformSecurityOperation(0x00u, 0xBEu, certData.map { it.toBer() }.mergeToArray().toPrintable())

		/**
		 * Tag AE: Input template for certificate verification (signed signature input consisting of non BER-TLV coded data)
		 *
		 * The certificate is not self-descriptive (P2 = 'AE'): the card retrieves a public key in the certificate
		 * either implicitly or explicitly by using the public key tag in a headerlist describing the content of
		 * the certificate.
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyCertificateNonBer(certData: UByteArray): PerformSecurityOperation =
			PerformSecurityOperation(0x00u, 0xAEu, certData.toPrintable())

		/**
		 * Tag BE: Input template for certificate verification (signed signature input consisting of BER-TLV coded data)
		 *
		 * The certificate is self-descriptive (P2 = 'BE'): the card retrieves a public key identified by its tag in
		 * the (recovered) certificate content.
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyCertificateBer(certData: UByteArray): PerformSecurityOperation =
			PerformSecurityOperation(0x00u, 0xBEu, certData.toPrintable())

		// TODO: encipher and decipher (ISO 7816-8)

		/**
		 * Padding indicator byte (see ISO/IEC 7816-4) followed by cryptogram (plain value not coded in BER-TLV)
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun encipherPlain(
			data: UByteArray,
			le: UShort = 0u,
		): PerformSecurityOperation = PerformSecurityOperation(0x86u, 0x80u, data.toPrintable(), le = le)

		/**
		 * Cryptogram, the plain value consisting of BER-TLV including SM related data objects
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun encipherTlvWithSmDo(
			data: UByteArray,
			le: UShort = 0u,
		): PerformSecurityOperation = PerformSecurityOperation(0x82u, 0x80u, data.toPrintable(), le = le)

		/**
		 * Cryptogram, the plain value consisting of BER-TLV, but not SM related data objects
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun encipherTlvWithoutSmDo(
			data: UByteArray,
			le: UShort = 0u,
		): PerformSecurityOperation = PerformSecurityOperation(0x84u, 0x80u, data.toPrintable(), le = le)

		/**
		 * Padding indicator byte (see ISO/IEC 7816-4) followed by cryptogram (plain value not coded in BER-TLV)
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun decipherPlain(
			data: UByteArray,
			le: UShort = 0u,
		): PerformSecurityOperation = PerformSecurityOperation(0x80u, 0x86u, data.toPrintable(), le = le)

		/**
		 * Cryptogram, the plain value consisting of BER-TLV including SM related data objects
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun decipherTlvWithSmDo(
			data: UByteArray,
			le: UShort = 0u,
		): PerformSecurityOperation = PerformSecurityOperation(0x80u, 0x82u, data.toPrintable(), le = le)

		/**
		 * Cryptogram, the plain value consisting of BER-TLV, but not SM related data objects
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		fun decipherTlvWithoutSmDo(
			data: UByteArray,
			le: UShort = 0u,
		): PerformSecurityOperation = PerformSecurityOperation(0x80u, 0x84u, data.toPrintable(), le = le)
	}
}
