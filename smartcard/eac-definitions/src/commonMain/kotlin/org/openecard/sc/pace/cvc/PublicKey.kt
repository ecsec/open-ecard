package org.openecard.sc.pace.cvc

import org.openecard.sc.pace.cvc.PublicKey.EcPublicKey.Companion.toEcPublicKey
import org.openecard.sc.pace.oid.TaObjectIdentifier
import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvConstructed
import org.openecard.sc.tlv.toObjectIdentifier

/**
 * Public Key Data Object according to TR-03110-3, Sec. D.3.
 */
sealed interface PublicKey {
	val identifier: ObjectIdentifier

	class EcPublicKey(
		override val identifier: ObjectIdentifier,
	) : PublicKey {
		companion object {
			fun TlvConstructed.toEcPublicKey(identifier: ObjectIdentifier): EcPublicKey {
				// TODO: parse contents
				return EcPublicKey(identifier)
			}
		}
	}

	companion object {
		@Throws(IllegalArgumentException::class)
		fun Tlv.toPublicKey(tag: Tag): PublicKey {
			require(this.tag == tag) { "The tag of the TLV ($tag) is not the expected tag." }
			return when (this) {
				is TlvConstructed -> {
					val id =
						this.child?.toObjectIdentifier()
							?: throw IllegalArgumentException("No object identifier in public key object")
					if (listOf(TaObjectIdentifier.id_TA_ECDSA).any { id.value.startsWith(it) }) {
						this.toEcPublicKey(id)
					} else {
						throw IllegalArgumentException("Unsupported PublicKey type with id=$id")
					}
				}
				else -> throw IllegalArgumentException("PublicKey TLV is not primitive")
			}
		}
	}
}
