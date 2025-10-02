import org.openecard.sc.pace.asn1.EfCardAccess.Companion.toEfCardAccess
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse
import org.openecard.utils.common.hex

object TestVars {
	object EgkG21 {
		@OptIn(ExperimentalUnsignedTypes::class)
		val efCaEgk = hex("31143012060a04007f0007020204020202010202010d")

		val encNonce =
			GeneralAuthenticateResponse.EncryptedNonce(
				hex("b6fecb1ec4185b112131368477daa803"),
			)
		val mapNonce =
			GeneralAuthenticateResponse.MapNonce(
				hex(
					@Suppress("ktlint:standard:max-line-length")
					"0476bf262a1c7a80b19007bb1e042a2d6819f35124f5deeb06cf152ffd7d7518147341e65654eeeaf5566cfe49e197a155bc0ce8d402c7523437da428fba912be4",
				),
			)
		val step2Nonce = hex("f695613cd06bd1618ff2373e0e217c79").toByteArray()

		val standardizedParameters =
			efCaEgk
				.toEfCardAccess()
				.paceInfo
				.first()
				.info
				.standardizedDomainParameters
	}
}
