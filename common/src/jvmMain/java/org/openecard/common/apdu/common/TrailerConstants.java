/****************************************************************************
 * Copyright (C) 2014-2015 ecsec GmbH.
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
 * @author Hans-Martin Haase
 */
public class TrailerConstants {

    /**
     * This class provides trailer constants which indicate a successful processing of an APDU.
     */
    public static class Success {
	
	/**
	 * Command processed normally. 
	 * No further qualification.<br>
	 *
	 * @return Code: 9000
	 */
	public static byte[] OK() { return new byte[] {(byte) 0x90, (byte) 0x00}; }
    }

    /**
     * This class provides trailer constants which indicate a warning while the processing of an APDU.
     */
    public static class Warning {

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: No information given.<br>
	 *
	 * @return @return Code: 6300
	 */
	public static byte[] NON_VOLATILE_MEMORY_HAS_CHANGED_NO_INFO() { return new byte[] {(byte) 0x63, (byte) 0x00}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: File filled up by the last write.<br>
	 *
	 * @return Code: 6381
	 */
	public static byte[] FILE_FILLED_UP() { return new byte[] {(byte) 0x63, (byte) 0x81}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 0.<br>
	 *
	 * @return Code: 63C0
	 */
	public static byte[] COUNTER_0() { return new byte[] {(byte) 0x63, (byte) 0xC0}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 1.<br>
	 *
	 * @return Code: 63C1
	 */
	public static byte[] COUNTER_1() { return new byte[] {(byte) 0x63, (byte) 0xC1}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 2.<br>
	 *
	 * @return Code: 63C2
	 */
	public static byte[] COUNTER_2() { return new byte[] {(byte) 0x63, (byte) 0xC2}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 3.<br>
	 *
	 * @return Code: 63C3
	 */
	public static byte[] COUNTER_3() { return new byte[] {(byte) 0x63, (byte) 0xC3}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 4.<br>
	 *
	 * @return Code: 63C4
	 */
	public static byte[] COUNTER_4() { return new byte[] {(byte) 0x63, (byte) 0xC4}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 5.<br>
	 *
	 * @return Code: 63C5
	 */
	public static byte[] COUNTER_5() { return new byte[] {(byte) 0x63, (byte) 0xC5}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 6.<br>
	 *
	 * @return Code: 63C6
	 */
	public static byte[] COUNTER_6() { return new byte[] {(byte) 0x63, (byte) 0xC6}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 7.<br>
	 *
	 * @return Code: 63C7
	 */
	public static byte[] COUNTER_7() { return new byte[] {(byte) 0x63, (byte) 0xC7}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 8.<br>
	 *
	 * @return Code: 63C8
	 */
	public static byte[] COUNTER_8() { return new byte[] {(byte) 0x63, (byte) 0xC8}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 9.<br>
	 *
	 * @return Code: 63C9
	 */
	public static byte[] COUNTER_9() { return new byte[] {(byte) 0x63, (byte) 0xC9}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 10.<br>
	 *
	 * @return Code: 63CA
	 */
	public static byte[] COUNTER_10() { return new byte[] {(byte) 0x63, (byte) 0xCA}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 11.<br>
	 *
	 * @return Code: 63CB
	 */
	public static byte[] COUNTER_11() { return new byte[] {(byte) 0x63, (byte) 0xCB}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 12.<br>
	 *
	 * @return Code: 63CC
	 */
	public static byte[] COUNTER_12() { return new byte[] {(byte) 0x63, (byte) 0xCC}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 13.<br>
	 *
	 * @return Code: 63CD
	 */
	public static byte[] COUNTER_13() { return new byte[] {(byte) 0x63, (byte) 0xCD}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 14.<br>
	 *
	 * @return Code: 63CE
	 */
	public static byte[] COUNTER_14() { return new byte[] {(byte) 0x63, (byte) 0xCE}; }

	/**
	 * State of non-volatile memory has changed.
	 * P2 indicates: Counter has set to 15.<br>
	 *
	 * @return Code: 63CF
	 */
	public static byte[] COUNTER_15() { return new byte[] {(byte) 0x63, (byte) 0xCF}; }
    }

    /**
     * This class provides trailer constants which indicate an error while the processing of an APDU.
     */
    public static class Error {

	/**
	 * Wrong length without further indication.
	 *
	 * @return Code: 6700
	 */
	public static byte[] WRONG_LENGTH() { return new byte[] {(byte) 0x67, (byte) 0x00}; }

	/**
	 * Command not allowed.
	 * P2 indicates: Security status not satisfied.<br>
	 *
	 * @return Code: 6982
	 */
	public static byte[] SECURITY_STATUS_NOT_SATISFIED() { return new byte[] {(byte) 0x69, (byte) 0x82}; }

	/**
	 * Wrong parameters P1-P2.
	 * P2 indicates: Incorrect parameters in the command data field.<br>
	 *
	 * @return Code: 6A80
	 */
	public static byte[] INCORRECT_COMMAND_DATA() { return new byte[] {(byte) 0x6A, (byte) 0x80}; }

	/**
	 * Wrong parameters P1-P2 further indication in SW2.
	 * P2 indicates: Incorrect parameters P1-P2.<br>
	 *
	 * @return Code: 6A86
	 */
	public static byte[] WRONG_P1_P2() { return new byte[] {(byte) 0x6A, (byte) 0x86}; }

	/**
	 * Wrong parameters P1-P2 no further indication.
	 *
	 * @return Code: 6B00
	 */
	public static byte[] WRONG_PARAMETERS_P1_2() { return new byte[] {(byte) 0x6B, (byte) 0x00}; }

	/**
	 * Instruction code not supported or invalid.
	 *
	 * @return Code: 6D00
	 */
	public static byte[] INS_NOT_SUPPORTED_OR_INVALID() { return new byte[] {(byte) 0x6D, (byte) 0x00}; }

    }

}
