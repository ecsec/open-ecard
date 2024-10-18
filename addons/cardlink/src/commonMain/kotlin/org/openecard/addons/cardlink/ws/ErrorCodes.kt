package org.openecard.addons.cardlink.ws


class ErrorCodes {

	enum class ClientCodes {
        // no CAN provided
		CAN_EMPTY,
		// CAN too long
		CAN_TOO_LONG,
		// CAN is not numeric
		CAN_NOT_NUMERIC		
		// interruption of channel establishment
		CAN_STEP_INTERRUPTED,
		// card was removed from antenna
		CARD_REMOVED,
		// CAN was not correct
		CAN_INCORRECT,
		// other PACE error occured
		OTHER_PACE_ERROR,
		// slot handle has become invalid
		INVALID_SLOT_HANDLE,
		// other NFC-error
		OTHER_NFC_ERROR,
		// Client timeout
		CLIENT_TIMEOUT,
		// other Client error
		OTHER_CLIENT_ERROR,
	}

	enum class CardLinkCodes(val statusCode: Int) {
		// Requested Entity Not found (TBD: ??? unklar, warum starten wir mit 1004? ... sollten wir z.B. in 5er 			   Schritten nummerieren, um bei Bedarf einfügen zu können?)
		NOT_FOUND(1004),
		// SICCT service returns an error
		SICCT_ERROR(1005),
		// Register eGK process is already ongoing 
		PROCESS_ALREADY_STARTED(1006),
		// Unknown Web Socket message
		UNKNOWN_WEBSOCKET_MESSAGE(100x), // TBD: fix numbering 
		// Invalid Web Socket message, can occur if required data are missing or message encoding is wrong
		INVALID_WEBSOCKET_MESSAGE(1007),
		// Received invalid data, for example no cardSessionId
		INVALID_REQUEST(1008),
		// Limit of 10 eGKs per session reached
		EGK_LIMIT_REACHED(1009),  // TBD: fix numbering 
		// session time has exceeded the permissible 15 minutes
		SESSION_EXPIRED(100x)
		// Expired eGK certificate
		EXPIRED_CERTIFICATE(1010),
		// Invalid eGK certificate (signature invalid, not a valid eGK certificate, ...)
		INVALID_CERTIFICATE(1011),
		// Mismatch between certificate validity periods of X.509 and CVC
		CERTIFICATE_VALIDITY_MISMATCH(100x), // TBD: fix numbering 
		// Invalid EF.GDO
		INVALID_GDO(1012),
		// Mismatch between ICCSN in CV certificate and EF.GDO
		ICCSN_MISMATCH(100x), // TBD: fix numbering 
		// Invalid EF.ATR
		INVALID_EF_ATR(1013),
		// Unable to send SMS for Tan validation
		UNABLE_TO_SEND_SMS(1014),
        // Validation of SMS TAN  failed
		TAN_VALIDATION_FAILED(100x), // TBD: fix numbering
		// Not admissible telephone number prefix, only +49... is allowed
		NOT_ADMISSIBLE_TEL_PREFIX(100x); 
		// Unknown error, probably an internal server error happened or used on an unknown result code
		UNKNOWN_ERROR(101x), // TBD: fix numbering

		companion object {
			private var codesByStatus: Map<Int, CardLinkCodes> = entries.associateBy { it.statusCode }

			fun byStatus(statusCode: Int): CardLinkCodes? {
				return codesByStatus[statusCode]
			}

		}
	}
}
