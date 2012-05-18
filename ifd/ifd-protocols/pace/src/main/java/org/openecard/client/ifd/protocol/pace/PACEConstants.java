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
 * Defines constants for the PACE protocol.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
class PACEConstants {

    // EF.CardAccess file identifier
    public static final short EF_CARDACCESS_FID = (short) 0x011C;
    // PACE password types
    public static final byte PASSWORD_MRZ = (byte) 0x01;
    public static final byte PASSWORD_CAN = (byte) 0x02;
    public static final byte PASSWORD_PIN = (byte) 0x03;
    public static final byte PASSWORD_PUK = (byte) 0x04;
    public static final String PIN_CHARSET = "ISO-8859-1";
    // MSE:Set AT error handling
    public static final short PASSWORD_SUSPENDED = (short) 0x63C1;
    public static final short PASSWORD_BLOCKED = (short) 0x63C0;
    public static final short PASSWORD_ERROR = (short) 0x6982;
    public static final short PASSWORD_DEACTIVATED = (short) 0x6283;
    // General Authenticate error handling
    public static final short SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    public static final short AUTHENTICATION_METHOD_BLOCKED = (short) 0x6983;
    public static final short REFERENCE_DATA_NOT_USABLE = (short) 0x6984;
    public static final short CONDITIONS_OF_USE_NOT_SATISFIED = (short) 0x6985;
    public static final short CMD_FAILED = (short) 0x6300;
    public static final short INCORRECT_PARA = (short) 0x6300;
}
