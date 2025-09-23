package org.openecard.sc.pace.cvc

import kotlinx.datetime.LocalDate
import org.openecard.sc.pace.cvc.DiscretionaryDataTemplate.Companion.toDiscretionaryDataTemplate
import org.openecard.sc.pace.oid.EacObjectIdentifier
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.TagClass
import org.openecard.sc.tlv.TlvException
import org.openecard.sc.tlv.toTlvBer

class AuthenticatedAuxiliaryData(
	private val discretionaryData: List<DiscretionaryDataTemplate>,
) {
	private val discretionaryDataReversed by lazy {
		discretionaryData.reversed()
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val ageVerification by lazy {
		val obj = discretionaryDataReversed.find { it.oid.value == EacObjectIdentifier.ID_DATE_OF_BIRTH }
		obj?.data?.convertDate()
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val documentValidityVerification by lazy {
		val obj = discretionaryDataReversed.find { it.oid.value == EacObjectIdentifier.ID_DATE_OF_EXPIRY }
		obj?.data?.convertDate()
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val communityIdVerification by lazy {
		val obj = discretionaryDataReversed.find { it.oid.value == EacObjectIdentifier.ID_COMMUNITY_ID }
		// TODO: check if this should be a string
		obj?.data
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val pseudonymousSignatureVerification by lazy {
		val obj = discretionaryDataReversed.find { it.oid.value == EacObjectIdentifier.ID_PSEUDONYMOUS_SIGNATURE_MESSAGE }
		obj?.data
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val dataGroupValidation by lazy {
		EacObjectIdentifier.dataGroupIds
			.mapNotNull { oid ->
				discretionaryDataReversed.find { it.oid.value == oid }
			}.associate { it.oid to it.data }
	}

	companion object {
		val AuthenticatedAuxiliaryDataTag = Tag(TagClass.APPLICATION, false, 7u)

		@OptIn(ExperimentalUnsignedTypes::class)
		@Throws(TlvException::class, IllegalArgumentException::class, NoSuchElementException::class)
		fun UByteArray.toAuthenticatedAuxiliariyData(): AuthenticatedAuxiliaryData {
			val tlv = requireNotNull(this.toTlvBer().tlv.asConstructed) { "Given tag is not constructed" }
			require(tlv.tag == AuthenticatedAuxiliaryDataTag) { "Authenticated Auxiliary Data object tag does is not correct" }

			val content = tlv.childList()
			val discretionaryData = content.map { it.toDiscretionaryDataTemplate() }

			return AuthenticatedAuxiliaryData(discretionaryData)
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		private fun UByteArray.convertDate(): LocalDate {
			val discretionaryData = this.toByteArray()
			require(discretionaryData.size == 8) { "Given value in discretionary data does not represent a date." }
			// the date is in the form YYYYMMDD and encoded as characters
			val yearStr = discretionaryData.decodeToString(0, 4)
			val year = yearStr.toInt()
			val monthStr = discretionaryData.decodeToString(4, 6)
			val month = monthStr.toInt()
			val dayStr = discretionaryData.decodeToString(6, 8)
			val day = dayStr.toInt()
			return LocalDate(year, month, day)
		}
	}
}
