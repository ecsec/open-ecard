#ifndef PCSC10_H
#define PCSC10_H


#pragma pack (1)

typedef struct _PIN_VERIFY_STRUCTURE
{
	uint8_t	bTimerOut;			// timeout in seconds (00 means use default timeout)
	uint8_t	bTimerOut2;			// timeout in seconds after first key stroke
	uint8_t	bmFormatString;		// formatting options
	uint8_t	bmPINBlockString;	// bits 7-4 bit size of PIN length in APDU, bits 3-0 PIN
	
	// block size in bytes after justification and formatting
	uint8_t	bmPINLengthFormat; // bits 7-5 RFU, bit 4 set if system units are bytes // clear if system units are bits,
	
	// bits 3-0 PIN length position in system units
	uint16_t	wPINMaxExtraDigit; // XXYY, where XX is minimum PIN size in digits,
	 
	// YY is maximum
	uint8_t	bEntryValidationCondition; // Conditions under which PIN entry should be
	
	// considered complete
	uint8_t	bNumberMessage;		// Number of messages to display for PIN verification
	uint16_t	wLangId;			// Language for messages
	uint8_t	bMsgIndex;			// Message index (should be 00)
	uint8_t	bTeoPrologue[3];	// T=1 I-block prologue field to use (fill with 00)
	uint32_t	ulDataLength;		// length of Data to be sent to the ICC
	uint8_t	abData[1];			// Data to send to the ICC
}PIN_VERIFY_STRUCTURE;

typedef struct _PIN_MODIFY_STRUCTURE
{
	uint8_t	bTimerOut;			// timeout in seconds (00 means use default timeout)
	uint8_t	bTimerOut2;			// timeout in seconds after first key stroke
	uint8_t	bmFormatString;		// formatting options USB_CCID_PIN_FORMAT_xxx)
	uint8_t	bmPINBlockString;	// bits 7-4 bit size of PIN length in APDU, bits 3-0 PIN
								// block size in bytes after justification and formatting
	uint8_t	bmPINLengthFormat;	// bits 7-5 RFU, bit 4 set if system units are bytes,
								// clear if system units are bits
								// bits 3-0 PIN length position in system units
								// bits, bits 3-0 PIN length position in system units
	uint8_t	bInsertionOffsetOld;// Insertion position offset in bytes for the current PIN
	uint8_t	bInsertionOffsetNew;// Insertion position offset in bytes for the new PIN
	uint16_t	wPINMaxExtraDigit;	// XXYY, where XX is minimum PIN size in digits,
								// YY is maximum
	uint8_t	bConfirmPIN;		// Flags governing need for confirmation of new PIN
	uint8_t	bEntryValidationCondition; // Conditions under which PIN entry should be
								// considered complete
	uint8_t	bNumberMessage;		// Number of messages to display for PIN verification
	uint16_t	wLangId;			// Language for messages
	uint8_t	bMsgIndex1;			// Index of 1st prompting message
	uint8_t	bMsgIndex2;			// Index of 2d prompting message
	uint8_t	bMsgIndex3;			// Index of 3d prompting message
	uint8_t	bTeoPrologue[3];	// T=1 I-block prologue field to use (fill with 00)
	uint32_t	ulDataLength;		// length of Data to be sent to the ICC
	uint8_t	abData[1];			// Data to send to the ICC
}PIN_MODIFY_STRUCTURE;
#pragma pack ()


#endif

