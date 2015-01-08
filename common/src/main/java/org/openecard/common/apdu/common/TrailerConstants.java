/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.common.apdu.common;


/**
 * Provides several subclasses for the different result trailers of an APDU.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class TrailerConstants {

    /**
     * This class provides trailer constants which indicate a successful processing of an APDU.
     */
    public static class Success {
	
	/**
	 * Command processed normally. 
	 * No further qualification.<br>
	 * Code: 9000
	 */
	public static final byte[] OK = new byte[] {(byte) 0x90, (byte) 0x00};
    }

    /**
     * This class provides trailer constants which indicate a warning while the processing of an APDU.
     */
    public static class Warning {

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: No information given.<br>
	 * Code: 6300
	 */
	public static final byte[] NON_VOLATILE_MEMORY_HAS_CHANGED_NO_INFO = new byte[] {(byte) 0x63, (byte) 0x00};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: File filled up by the last write.<br>
	 * Code: 6381
	 */
	public static final byte[] FILE_FILLED_UP = new byte[] {(byte) 0x63, (byte) 0x81};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 0.<br>
	 * Code: 63C0
	 */
	public static final byte[] COUNTER_0 = new byte[] {(byte) 0x63, (byte) 0xC0};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 1.<br>
	 * Code: 63C1
	 */
	public static final byte[] COUNTER_1 = new byte[] {(byte) 0x63, (byte) 0xC1};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 2.<br>
	 * Code: 63C2
	 */
	public static final byte[] COUNTER_2 = new byte[] {(byte) 0x63, (byte) 0xC2};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 3.<br>
	 * Code: 63C3
	 */
	public static final byte[] COUNTER_3 = new byte[] {(byte) 0x63, (byte) 0xC3};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 4.<br>
	 * Code: 63C4
	 */
	public static final byte[] COUNTER_4 = new byte[] {(byte) 0x63, (byte) 0xC4};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 5.<br>
	 * Code: 63C5
	 */
	public static final byte[] COUNTER_5 = new byte[] {(byte) 0x63, (byte) 0xC5};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 6.<br>
	 * Code: 63C6
	 */
	public static final byte[] COUNTER_6 = new byte[] {(byte) 0x63, (byte) 0xC6};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 7.<br>
	 * Code: 63C7
	 */
	public static final byte[] COUNTER_7 = new byte[] {(byte) 0x63, (byte) 0xC7};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 8.<br>
	 * Code: 63C8
	 */
	public static final byte[] COUNTER_8 = new byte[] {(byte) 0x63, (byte) 0xC8};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 9.<br>
	 * Code: 63C9
	 */
	public static final byte[] COUNTER_9 = new byte[] {(byte) 0x63, (byte) 0xC9};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 10.<br>
	 * Code: 63CA
	 */
	public static final byte[] COUNTER_10 = new byte[] {(byte) 0x63, (byte) 0xCA};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 11.<br>
	 * Code: 63CB
	 */
	public static final byte[] COUNTER_11 = new byte[] {(byte) 0x63, (byte) 0xCB};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 12.<br>
	 * Code: 63CC
	 */
	public static final byte[] COUNTER_12 = new byte[] {(byte) 0x63, (byte) 0xCC};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 13.<br>
	 * Code: 63CD
	 */
	public static final byte[] COUNTER_13 = new byte[] {(byte) 0x63, (byte) 0xCD};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 14.<br>
	 * Code: 63CE
	 */
	public static final byte[] COUNTER_14 = new byte[] {(byte) 0x63, (byte) 0xCE};

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 15.<br>
	 * Code: 63CF
	 */
	public static final byte[] COUNTER_15 = new byte[] {(byte) 0x63, (byte) 0xCF};
    }

    /**
     * This class provides trailer constants which indicate an error while the processing of an APDU.
     */
    public static class Error {

	/**
	 * Wrong length without further indication.
	 * Code: 6700
	 */
	public static final byte[] WRONG_LENGTH = new byte[] {(byte) 0x67, (byte) 0x00};

	/**
	 * Command not allowed.
	 * P2 indicates: Security status not satisfied.<br>
	 * Code: 6982
	 */
	public static final byte[] SECURITY_STATUS_NOT_SATISFIED = new byte[] {(byte) 0x69, (byte) 0x82};

	/**
	 * Wrong parameters P1-P2.
	 * P2 indicates: Incorrect parameters in the command data field.<br>
	 * Code: 6A80
	 */
	public static final byte[] INCORRECT_COMMAND_DATA = new byte[] {(byte) 0x6A, (byte) 0x80};

	/**
	 * Wrong parameters P1-P2 further indication in SW2.
	 * P2 indicates: Incorrect parameters P1-P2.<br>
	 * Code: 6A86
	 */
	public static final byte[] WRONG_P1_P2 = new byte[] {(byte) 0x6A, (byte) 0x86};

	/**
	 * Wrong parameters P1-P2 no further indication.
	 * Code: 6B00
	 */
	public static final byte[] WRONG_PARAMETERS_P1_2 = new byte[] {(byte) 0x6B, (byte) 0x00};

	/**
	 * Instruction code not supported or invalid.
	 * Code: 6D00
	 */
	public static final byte[] INS_NOT_SUPPORTED_OR_INVALID = new byte[] {(byte) 0x6D, (byte) 0x00};

    }
    
}
