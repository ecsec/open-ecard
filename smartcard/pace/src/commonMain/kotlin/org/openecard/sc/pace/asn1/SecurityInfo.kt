package org.openecard.sc.pace.asn1

import dev.whyoleg.cryptography.serialization.asn1.Der
import dev.whyoleg.cryptography.serialization.asn1.ObjectIdentifier
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.decodeFromByteArray
import org.openecard.sc.pace.asn1.ChipAuthenticationDomainParameterInfo.Companion.toChipAuthenticationDomainParameterInfo
import org.openecard.sc.pace.asn1.ChipAuthenticationInfo.Companion.toChipAuthenticationInfo
import org.openecard.sc.pace.asn1.ChipAuthenticationPublicKeyInfo.Companion.toChipAuthenticationPublicKeyInfo
import org.openecard.sc.pace.asn1.PaceDomainParameterInfo.Companion.toPaceDomainParameterInfo
import org.openecard.sc.pace.asn1.PaceInfo.Companion.toPaceInfo
import org.openecard.sc.pace.asn1.PsPublicKeyInfo.Companion.toPsPublicKeyInfo
import org.openecard.sc.pace.asn1.PsaInfo.Companion.toPsaInfo
import org.openecard.sc.tlv.Tlv

private val log = KotlinLogging.logger { }

sealed class SecurityInfo(
	val protocol: ObjectIdentifier,
) {
	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun Tlv.toSecurityInfo(): Set<SecurityInfo> {
			// the tlv is a set of SecurityInfos
			require(this.tag.tagNumWithClass == 0x31uL) { "SecurityInfos is not a set tag ($tag)" }

			// process all elements
			val secInfoTlvs =
				this.asConstructed?.childList()
					?: throw IllegalArgumentException("SecurityInfos set is not a constructed tag")
			return secInfoTlvs
				.map { secInfoTlv ->
					require(secInfoTlv.tag.tagNumWithClass == 0x30uL) { "SecurityInfo is not a sequence tag" }
					val childTags =
						secInfoTlv.asConstructed?.childList() ?: throw IllegalArgumentException("SecurityInfo is not a constructed tag")
					require(childTags.size >= 2 && childTags.size <= 3)

					// read protocol object identifier
					val protocol: ObjectIdentifier = Der.decodeFromByteArray(childTags[0].toBer().toByteArray())

					if (PaceInfo.isResponsible(protocol)) {
						childTags.toPaceInfo(protocol)
					} else if (PaceDomainParameterInfo.isResponsible(protocol)) {
						childTags.toPaceDomainParameterInfo(protocol)
					} else if (ChipAuthenticationInfo.isResponsible(protocol)) {
						childTags.toChipAuthenticationInfo(protocol)
					} else if (ChipAuthenticationPublicKeyInfo.isResponsible(protocol)) {
						childTags.toChipAuthenticationPublicKeyInfo(protocol)
					} else if (ChipAuthenticationDomainParameterInfo.isResponsible(protocol)) {
						childTags.toChipAuthenticationDomainParameterInfo(protocol)
					} else if (PsaInfo.isResponsible(protocol)) {
						childTags.toPsaInfo(protocol)
					} else if (PsPublicKeyInfo.isResponsible(protocol)) {
						childTags.toPsPublicKeyInfo(protocol)
					} else {
						log.info { "Unknown security info entry with OID=${protocol.value}" }
						Unknown(protocol, childTags)
					}
				}.toSet()
		}
	}

	class Unknown(
		protocol: ObjectIdentifier,
		val children: List<Tlv>,
	) : SecurityInfo(protocol)
}
