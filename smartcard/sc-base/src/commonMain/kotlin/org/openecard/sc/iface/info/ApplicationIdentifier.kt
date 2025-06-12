package org.openecard.sc.iface.info

import org.openecard.sc.tlv.Tlv

/**
 * Application Identifier according to ISO-7816-4, Sec.8.1.1.2.2.
 */
class ApplicationIdentifier
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val aid: UByteArray,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		val category by lazy { Category.forAid(aid) }

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromDataObject(tlv: Tlv): ApplicationIdentifier? =
				if (tlv.tag.tagNumWithClass == 0x4FuL) {
					ApplicationIdentifier(tlv.contentAsBytesBer)
				} else {
					null
				}

			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromDataObjects(tlvs: List<Tlv>): List<ApplicationIdentifier> =
				tlvs
					.filter {
						it.tag.tagNumWithClass == 0x4FuL
					}.map { ApplicationIdentifier(it.contentAsBytesBer) }
		}

		enum class Category {
			/**
			 * Reserved for backward compatibility with ISO/IEC 7812-1 (see annex D)
			 */
			BACKWARD_COMPATIBLE,

			/**
			 * International registration of application providers according to ISO/IEC 7816-5
			 */
			INTERNATIONAL,

			/**
			 * Reserved for future use by ISO/IEC JTC 1/SC 17
			 */
			FUTURE_USE,

			/**
			 * National (ISO 3166-1) registration of application providers according to ISO/IEC 7816-5
			 */
			NATIONAL,

			/**
			 * Identification of a standard by an object identifier according to ISO/IEC 8825-1
			 */
			STANDARD,

			/**
			 * No registration of application providers
			 */
			PROPRIETARY,
			;

			companion object {
				@OptIn(ExperimentalUnsignedTypes::class)
				fun forAid(aid: UByteArray): Category {
					val catNibble = aid[0].toInt() shr 4
					return if (catNibble >= 0 && catNibble <= 9) {
						BACKWARD_COMPATIBLE
					} else if (catNibble == 0xA) {
						INTERNATIONAL
					} else if (catNibble == 0xB || catNibble == 0xC) {
						FUTURE_USE
					} else if (catNibble == 0xD) {
						NATIONAL
					} else if (catNibble == 0xE) {
						STANDARD
					} else if (catNibble == 0xF) {
						PROPRIETARY
					} else {
						// we covered all branches, but if someone changes the code, this captures programming mistakes
						throw RuntimeException("Internal logic error in AID category determination")
					}
				}
			}
		}
	}
