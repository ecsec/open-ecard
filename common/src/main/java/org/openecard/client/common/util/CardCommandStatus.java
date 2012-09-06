/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.common.util;


/**
 * Resolves RAPDU status words to human readable messages.
 * Cp. ISO-7816-4 sec. 5.1.3 Status Bytes
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CardCommandStatus {

    private static final String defaultMsg = "Unknown status word (possibly proprietary).";
    private static final String sw62 = "State of non-volatile memory unchanged.";
    private static final String sw63 = "State of non-volatile memory changed.";
    private static final String sw64 = "State of non-volatile memory unchanged.";
    private static final String sw65 = "State of non-volatile memory changed.";
    private static final String sw66 = "Security related issues.";
    private static final String sw67 = "Wrong length without further indication.";
    private static final String sw68 = "Functions in CLA not supported.";
    private static final String sw69 = "Command not allowed.";
    private static final String sw6A = "Wrong parameters P1-P2.";
    private static final String sw6B = "Wrong parameters P1-P2.";
    private static final String sw6C = "Wrong length field.";
    private static final String sw6D = "Instruction code not supported or invalid.";
    private static final String sw6E = "Class not supported.";
    private static final String sw6F = "No precise diagnosis.";


    public static String getMessage(byte[] status) {
	String msg = defaultMsg;
	switch (status[0]) {
	case (byte)0x90:
	    switch (status[1]) {
	    case 0x00: msg = "No error."; break;
	    }
	    break;

	case 0x61: msg = "No error, but " + (0xFF & status[1]) + " bytes left to read."; break;

	case 0x62:
	    switch (status[1]) {
	    case (byte)0x00: msg = sw62 + " No further information given."; break;
	    case (byte)0x81: msg = sw62 + " Part of returned data may be corrupted."; break;
	    case (byte)0x82: msg = sw62 + " End of file reached before reading requested number of bytes."; break;
	    case (byte)0x83: msg = sw62 + " Selected file deactivated."; break;
	    case (byte)0x84: msg = sw62 + " File control information not formatted according to ISO/IEC 7816-4."; break;
	    case (byte)0x85: msg = sw62 + " Selected file in termination state."; break;
	    case (byte)0x86: msg = sw62 + " No input data available from a sensor on the card."; break;
	    default:
		if (status[1] >= 0x02 && status[1] <= 0x80) {
		    msg = sw62 + " Triggering by the card (see ISO/IEC 7816-4 8.6.1).";
		}
	    }
	    break;

	case 0x63:
	    switch (status[1]) {
	    case (byte)0x00: msg = sw63 + " No further information given."; break;
	    case (byte)0x81: msg = sw63 + " File filled up by last write."; break;
	    default:
		if (status[1] >= 0xC0 && status[1] <= 0xCF) {
		    msg = sw63 + " Counter is " + (0x0F & status[1]) + ".";
		}
	    }
	    break;

	case 0x64:
	    switch (status[1]) {
	    case (byte)0x00: msg = sw64 + " Execution error."; break;
	    case (byte)0x01: msg = sw64 + " Immediate response required by the card."; break;
	    default:
		if (status[1] >= 0x02 && status[1] <= 0x80) {
		    msg = sw64 + " Triggered by the card (see ISO/IEC 7816-4 8.6.1).";
		}
	    }
	    break;

	case 0x65:
	    switch (status[1]) {
	    case (byte)0x00: msg = sw65 + " No information given."; break;
	    case (byte)0x81: msg = sw65 + " Memory failure."; break;
	    }
	    break;

	case 0x66: msg = sw66; break;

	case 0x67:
	    switch (status[1]) {
	    case (byte)0x00: msg = sw67; break;
	    }
	    break;

	case 0x68:
	    switch (status[1]) {
	    case (byte)0x00: msg = sw68 + " No information given."; break;
	    case (byte)0x81: msg = sw68 + " Logical channel not supported."; break;
	    case (byte)0x82: msg = sw68 + " Secure messaging not supported."; break;
	    case (byte)0x83: msg = sw68 + " Last command of the chain expected."; break;
	    case (byte)0x84: msg = sw68 + " Command chaining not supported."; break;
	    }
	    break;

	case 0x69:
	    switch (status[1]) {
	    case (byte)0x00: msg = sw69 + " No information given."; break;
	    case (byte)0x81: msg = sw69 + " Command incompatible with file structure."; break;
	    case (byte)0x82: msg = sw69 + " Security status not satisfied."; break;
	    case (byte)0x83: msg = sw69 + " Authentication method blocked."; break;
	    case (byte)0x84: msg = sw69 + " Reference data not usable."; break;
	    case (byte)0x85: msg = sw69 + " Conditions of use not satisfied."; break;
	    case (byte)0x86: msg = sw69 + " Command not allowed (no current EF)."; break;
	    case (byte)0x87: msg = sw69 + " Expected secure messaging data objects missing."; break;
	    case (byte)0x88: msg = sw69 + " Incorrect secure messaging data objects."; break;
	    }
	    break;

	case 0x6A:
	    switch (status[1]) {
	    case (byte)0x00: msg = sw6A + " No information given."; break;
	    case (byte)0x80: msg = sw6A + " Incorrect parameters in the command data field."; break;
	    case (byte)0x81: msg = sw6A + " Function not supported."; break;
	    case (byte)0x82: msg = sw6A + " File or application not found."; break;
	    case (byte)0x83: msg = sw6A + " Record not found."; break;
	    case (byte)0x84: msg = sw6A + " Not enough memory space in the file."; break;
	    case (byte)0x85: msg = sw6A + " Command length inconsistent with TLV structure."; break;
	    case (byte)0x86: msg = sw6A; break; // detailed message has no different spec than base message
	    case (byte)0x87: msg = sw6A + " Command length inconsistent with parameters P1-P2."; break;
	    case (byte)0x88: msg = sw6A + " Referenced data or reference data not found."; break;
	    case (byte)0x89: msg = sw6A + " File already exists."; break;
	    case (byte)0x8A: msg = sw6A + " DF name already exists."; break;
	    }
	    break;

	case (byte)0x6B:
	    switch (status[1]) {
	    case 0x00: msg = sw6B; break;
	    }
	    break;

	case (byte)0x6C: msg = sw6C + " " + (0xFF & status[1]) + " bytes available."; break;

	case (byte)0x6D:
	    switch (status[1]) {
	    case 0x00: msg = sw6D; break;
	    }
	    break;

	case (byte)0x6E:
	    switch (status[1]) {
	    case 0x00: msg = sw6E; break;
	    }
	    break;

	case (byte)0x6F:
	    switch (status[1]) {
	    case 0x00: msg = sw6F; break;
	    }
	    break;
	}

	// append status code
	msg += " (Code: " + ByteUtils.toHexString(status) + ")";

	return msg;
    }

}
