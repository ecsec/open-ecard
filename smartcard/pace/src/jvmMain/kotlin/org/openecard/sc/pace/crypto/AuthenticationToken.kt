package org.openecard.sc.pace.crypto

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.feature.PaceResultCode
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponseTags
import org.openecard.sc.pace.asn1.PaceInfo
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.TagClass
import org.openecard.sc.tlv.buildTlv
import org.openecard.sc.tlv.tlvStandard
import org.openecard.utils.common.removeLeadingZeros

private val log = KotlinLogging.logger { }

class AuthenticationToken
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val tokenValue: UByteArray,
	) {
		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun generate(
				paceInfo: PaceInfo,
				keyMac: ByteArray,
				key: ByteArray,
			): AuthenticationToken {
				val macData = getMacObject(paceInfo, key)
				val signer = cmacKey(key).signer()
				signer.update(macData)
				val mac = signer.sign()
				return AuthenticationToken(mac.sliceArray(0 until 8).toUByteArray())
			}

			@OptIn(ExperimentalUnsignedTypes::class)
			private fun getMacObject(
				paceInfo: PaceInfo,
				key: ByteArray,
			): ByteArray {
				val authObject =
					buildTlv(Tag(TagClass.APPLICATION, false, 0x49u)) {
						generic(paceInfo.protocol.tlvStandard)
						primitive(
							GeneralAuthenticateResponseTags.authenticationToken,
							key.toUByteArray().removeLeadingZeros(),
						)
					}

				return authObject.toBer().toByteArray()
			}
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		fun verify(authResponse: GeneralAuthenticateResponse.AuthenticationToken) {
			// compare received and calculated token
			if (!tokenValue.contentEquals(authResponse.at)) {
				log.error { "Received authentication token does not match the calculated value" }
				throw PaceError(PaceResultCode.WRONG_AUTH_TOKEN, null)
			}
		}
	}
