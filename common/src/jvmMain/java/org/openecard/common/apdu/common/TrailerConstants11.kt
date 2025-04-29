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
 */
package org.openecard.common.apdu.common


/**
 * Provides several subclasses for the different result trailers of an APDU.
 *
 * @author Hans-Martin Haase
 */
class TrailerConstants {
    /**
     * This class provides trailer constants which indicate a successful processing of an APDU.
     */
    object Success {
        /**
         * Command processed normally.
         * No further qualification.<br></br>
         *
         * @return Code: 9000
         */
        @JvmStatic
        fun OK(): ByteArray {
            return byteArrayOf(0x90.toByte(), 0x00.toByte())
        }
    }

    /**
     * This class provides trailer constants which indicate a warning while the processing of an APDU.
     */
    object Warning {
        /**
         * State of non-volatile memory has changed.
         * P2 indicates: No information given.<br></br>
         *
         * @return @return Code: 6300
         */
        fun NON_VOLATILE_MEMORY_HAS_CHANGED_NO_INFO(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0x00.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: File filled up by the last write.<br></br>
         *
         * @return Code: 6381
         */
        fun FILE_FILLED_UP(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0x81.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 0.<br></br>
         *
         * @return Code: 63C0
         */
        fun COUNTER_0(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC0.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 1.<br></br>
         *
         * @return Code: 63C1
         */
        fun COUNTER_1(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC1.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 2.<br></br>
         *
         * @return Code: 63C2
         */
        fun COUNTER_2(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC2.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 3.<br></br>
         *
         * @return Code: 63C3
         */
        fun COUNTER_3(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC3.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 4.<br></br>
         *
         * @return Code: 63C4
         */
        fun COUNTER_4(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC4.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 5.<br></br>
         *
         * @return Code: 63C5
         */
        fun COUNTER_5(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC5.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 6.<br></br>
         *
         * @return Code: 63C6
         */
        fun COUNTER_6(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC6.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 7.<br></br>
         *
         * @return Code: 63C7
         */
        fun COUNTER_7(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC7.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 8.<br></br>
         *
         * @return Code: 63C8
         */
        fun COUNTER_8(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC8.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 9.<br></br>
         *
         * @return Code: 63C9
         */
        fun COUNTER_9(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xC9.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 10.<br></br>
         *
         * @return Code: 63CA
         */
        fun COUNTER_10(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xCA.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 11.<br></br>
         *
         * @return Code: 63CB
         */
        fun COUNTER_11(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xCB.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 12.<br></br>
         *
         * @return Code: 63CC
         */
        fun COUNTER_12(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xCC.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 13.<br></br>
         *
         * @return Code: 63CD
         */
        fun COUNTER_13(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xCD.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 14.<br></br>
         *
         * @return Code: 63CE
         */
        fun COUNTER_14(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xCE.toByte())
        }

        /**
         * State of non-volatile memory has changed.
         * P2 indicates: Counter has set to 15.<br></br>
         *
         * @return Code: 63CF
         */
        fun COUNTER_15(): ByteArray {
            return byteArrayOf(0x63.toByte(), 0xCF.toByte())
        }
    }

    /**
     * This class provides trailer constants which indicate an error while the processing of an APDU.
     */
    object Error {
        /**
         * Wrong length without further indication.
         *
         * @return Code: 6700
         */
        fun WRONG_LENGTH(): ByteArray {
            return byteArrayOf(0x67.toByte(), 0x00.toByte())
        }

        /**
         * Command not allowed.
         * P2 indicates: Security status not satisfied.<br></br>
         *
         * @return Code: 6982
         */
        fun SECURITY_STATUS_NOT_SATISFIED(): ByteArray {
            return byteArrayOf(0x69.toByte(), 0x82.toByte())
        }

        /**
         * Wrong parameters P1-P2.
         * P2 indicates: Incorrect parameters in the command data field.<br></br>
         *
         * @return Code: 6A80
         */
        fun INCORRECT_COMMAND_DATA(): ByteArray {
            return byteArrayOf(0x6A.toByte(), 0x80.toByte())
        }

        /**
         * Wrong parameters P1-P2 further indication in SW2.
         * P2 indicates: Incorrect parameters P1-P2.<br></br>
         *
         * @return Code: 6A86
         */
        @JvmStatic
        fun WRONG_P1_P2(): ByteArray {
            return byteArrayOf(0x6A.toByte(), 0x86.toByte())
        }

        /**
         * Wrong parameters P1-P2 no further indication.
         *
         * @return Code: 6B00
         */
        fun WRONG_PARAMETERS_P1_2(): ByteArray {
            return byteArrayOf(0x6B.toByte(), 0x00.toByte())
        }

        /**
         * Instruction code not supported or invalid.
         *
         * @return Code: 6D00
         */
        fun INS_NOT_SUPPORTED_OR_INVALID(): ByteArray {
            return byteArrayOf(0x6D.toByte(), 0x00.toByte())
        }
    }
}
