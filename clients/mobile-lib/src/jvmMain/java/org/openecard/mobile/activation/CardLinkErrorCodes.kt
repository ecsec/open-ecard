package org.openecard.mobile.activation


class CardLinkErrorCodes {

	enum class ClientCodes {
        // no CAN provided
		CAN_EMPTY,
		// CAN too long
		CAN_TOO_LONG,
		// CAN is not numeric
		CAN_NOT_NUMERIC,
		// interruption of channel establishment
		CAN_STEP_INTERRUPTED,
		// card was removed from antenna
		CARD_REMOVED,
		// CAN was not correct
		CAN_INCORRECT,
		// other PACE error occurred
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
		// Requested Entity Not found
		NOT_FOUND(1004),
		// SICCT service returns an error
		SICCT_ERROR(1005),
		// Register eGK process is already ongoing 
		PROCESS_ALREADY_STARTED(1006),
		// Unknown Web Socket message
		UNKNOWN_WEBSOCKET_MESSAGE(1007),
		// Invalid Web Socket message, can occur if required data are missing or message encoding is wrong
		INVALID_WEBSOCKET_MESSAGE(1008),
		// Limit of 10 eGKs per session reached
		EGK_LIMIT_REACHED(1009),
		// session time has exceeded the permissible 15 minutes
		SESSION_EXPIRED(1010),
		// Expired eGK certificate
		EXPIRED_CERTIFICATE(1011),
		// Invalid eGK certificate (signature invalid, not a valid eGK certificate, ...)
		INVALID_CERTIFICATE(1012),
		// Mismatch between certificate validity periods of X.509 and CVC
		CERTIFICATE_VALIDITY_MISMATCH(1013),
		// Invalid EF.GDO
		INVALID_GDO(1014),
		// Mismatch between ICCSN in CV certificate and EF.GDO
		ICCSN_MISMATCH(1015),
		// Invalid EF.ATR
		INVALID_EF_ATR(1016),
		// Unable to send SMS for Tan validation
		UNABLE_TO_SEND_SMS(1017),
		// Not admissible telephone number prefix, only +49... is allowed
		NOT_ADMISSIBLE_TEL_PREFIX(1018),
		// Unknown error, probably an internal server error happened or used on an unknown result code
		UNKNOWN_ERROR(1019),
		// Phone number is blocked
		NUMBER_BLOCKED(1020),
		// Tan has expired
		TAN_EXPIRED(1021),
		// Tan is incorrect
		TAN_INCORRECT(1022),
		// Tan retry limit exceeded
		TAN_RETRY_LIMIT_EXCEEDED(1023),
		// If the client does not receive an APDU message from the CardLink service
		SERVER_TIMEOUT(1024);

		companion object {
			private var codesByStatus: Map<Int, CardLinkCodes> = entries.associateBy { it.statusCode }

			fun byStatus(statusCode: Int): CardLinkCodes? {
				return codesByStatus[statusCode]
			}
		}
	}
}
