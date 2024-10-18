package org.openecard.addons.cardlink.ws


class ErrorCodes {

	enum class ClientCodes {
		CAN_EMPTY,
		CAN_STEP_INTERRUPTED,
		CARD_REMOVED,
		CAN_INCORRECT,
		INVALID_SLOT_HANDLE,
	}

	enum class CardLinkCodes(val statusCode: Int) {
		// Requested Entity Not found
		NOT_FOUND(1004),
		// SICCT returns an error
		SICCT_ERROR(1005),
		// Register eGK process is already ongoing
		PROCESS_ALREADY_STARTED(1006),
		// Unsupported or unknown Web Socket message, can occur if required data are missing
		UNSUPPORTED_ENVELOPE(1007),
		// Received invalid data, for example no cardSessionId
		INVALID_REQUEST(1008),
		// Limit of 10 eGKs per session reached
		EGK_LIMIT_REACHED(1009),
		// Expired eGK certificate
		EXPIRED_CERTIFICATE(1010),
		// Invalid eGK certificate (signature invalid, not a eGK certificate, ...)
		INVALID_CERTIFICATE(1011),
		// Invalid EG.GDO
		INVALID_GDO(1012),
		// Invalid EF.ATR
		INVALID_EF_ATR(1013),
		// Unable to send SMS for Tan validation
		UNABLE_TO_SEND_SMS(1014),
		// Unknown error, probably an internal server error happened or used on an unknown result code
		UNKNOWN_ERROR(1015);

		companion object {
			private var codesByStatus: Map<Int, CardLinkCodes> = entries.associateBy { it.statusCode }

			fun byStatus(statusCode: Int): CardLinkCodes? {
				return codesByStatus[statusCode]
			}

		}
	}
}
