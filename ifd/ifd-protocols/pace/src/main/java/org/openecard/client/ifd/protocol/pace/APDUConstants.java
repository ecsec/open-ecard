/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.ifd.protocol.pace;

/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
@Deprecated
public class APDUConstants {

    public static final byte NULL = (byte) 0x00;
    public static final byte SELECT_INS = (byte) 0xA4;
    public static final byte READ_BINARY_INS = (byte) 0xB0;
    public static final short SW_NO_ERROR = (short) 0x9000;
    public static final short SW_BYTES_REMAINING_00 = (short) 0x6100;
    public static final short SW_WRONG_LENGTH = (short) 0x6700;
    public static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    public static final short SW_FILE_INVALID = (short) 0x6983;
    public static final short SW_DATA_INVALID = (short) 0x6984;
    public static final short SW_CONDITIONS_NOT_SATISFIED = (short) 0x6985;
    public static final short SW_COMMAND_NOT_ALLOWED = (short) 0x6986;
    public static final short SW_APPLET_SELECT_FAILED = (short) 0x6999;
    public static final short SW_WRONG_DATA = (short) 0x6A80;
    public static final short SW_FUNC_NOT_SUPPORTED = (short) 0x6A81;
    public static final short SW_FILE_NOT_FOUND = (short) 0x6A82;
    public static final short SW_RECORD_NOT_FOUND = (short) 0x6A83;
    public static final short SW_INCORRECT_P1P2 = (short) 0x6A86;
    public static final short SW_WRONG_P1P2 = (short) 0x6B00;
    public static final short SW_CORRECT_LENGTH_00 = (short) 0x6C00;
    public static final short SW_INS_NOT_SUPPORTED = (short) 0x6D00;
    public static final short SW_CLA_NOT_SUPPORTED = (short) 0x6E00;
    public static final short SW_UNKNOWN = (short) 0x6F00;
    public static final short SW_FILE_FULL = (short) 0x6A84;
    public static final short SW_EOF = (short) 0x6282;
    public static final short CMD_SUCCESSFUL = (short) 0x9000;
    /*
     * MSE:Set AT (TR-03110 2.05 Section B.11.1)
     */
    public static final byte MSESet_AT_INS = (byte) 0x22;
    public static final short MSESet_AT_PARA_PACE = (short) 0xC1A4;
    public static final short MSESet_AT_PARA_CA = (short) 0x41A4;
    public static final short MSESet_AT_PARA_TA = (short) 0x81A4;
    /*
     * General Authenticate (TR-03110 2.05 Section B.11.2)
     */
    public static final byte GENERAL_AUTH_INS = (byte) 0x86;
    public static final short GENERAL_AUTH_PARA = (short) 0x0000;
    public static final byte GENERAL_AUTH_DATA = (byte) 0x7C;
    /*
     * MSE:Set DST (TR-03110 2.05 Section B.11.4)
     */
    public static final byte MSESet_DST_INS = (byte) 0x22;
    public static final short MSESet_DST_PARA = (short) 0x81B6;
    /*
     * PSO:Verify Certificate (TR-03110 2.05 Section B.11.5)
     */
    public static final byte PSOVC_INS = (byte) 0x2A;
    public static final short PSOVC_PARA = (short) 0x00BE;
    public static final short PSOVC_DATA_CertBody = (short) 0x7F4E;
    public static final short PSOVC_DATA_CertSig = (short) 0x5F37;
    /*
     * Get Challenge (TR-03110 2.05 Section B.11.6)
     */
    public static final byte GET_CHALLENGE_INS = (byte) 0x84;
    public static final short GET_CHALLENGE_PARA = (short) 0x0000;
    /*
     * External Authenticate (TR-03110 2.05 Section B.11.7)
     */
    public static final byte EXTERNAL_AUTH_INS = (byte) 0x82;
    public static final short EXTERNAL_AUTH_PARA = (short) 0x0000;
    /*
     * Reset Retry Counter (TR-03110 2.05 Section B.11.9)
     */
    public static final byte RESET_RETRY_COUNTER_INS = (byte) 0x2C;
    /*
     * MSE:Set AT error handling
     */
    public static final short DATA_NOT_FOUND = (short) 0x6A88;
    public static final short PASSWORD_SUSPENDED = (short) 0x63C1;
    public static final short PASSWORD_BLOCKED = (short) 0x63C0;
    public static final short PASSWORD_ERROR = (short) 0x6982;
    public static final short PASSWORD_DEACTIVATED = (short) 0x6283;
    /*
     * General Authenticate error handling
     */
    public static final short SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    public static final short AUTHENTICATION_METHOD_BLOCKED = (short) 0x6983;
    public static final short REFERENCE_DATA_NOT_USABLE = (short) 0x6984;
    public static final short CONDITIONS_OF_USE_NOT_SATISFIED = (short) 0x6985;
    public static final short CMD_FAILED = (short) 0x6300;
    public static final short INCORRECT_PARA = (short) 0x6300;
}
