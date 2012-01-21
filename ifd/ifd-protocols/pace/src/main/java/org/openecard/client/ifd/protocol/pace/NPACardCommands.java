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

import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.IntegerUtils;
import org.openecard.client.common.util.ShortUtils;
import org.openecard.client.crypto.common.asn1.eac.CASecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.TASecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.oid.CAObjectIdentifier;
import org.openecard.client.crypto.common.asn1.eac.oid.TAObjectIdentifier;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.openecard.client.crypto.common.asn1.utils.TLV;

/**
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
@Deprecated
public class NPACardCommands {

    private static final byte NULL = (byte) 0x00;
    private static final byte FF = (byte) 0xFF;

    /**
     * Creates a new Get Challenge APDU.
     * @return Get Challenge APDU
     */
    public static byte[] getChallenge() {
        return getChallenge(8);
    }

    /**
     * Creates a new Get Challenge APDU.
     * @param length Length of the expected random
     * @return Get Challenge APDU
     */
    public static byte[] getChallenge(int length) {
        byte le = (byte) (length & FF);
        byte[] apdu = new byte[]{NULL, APDUConstants.GET_CHALLENGE_INS, NULL, NULL, le};

        return apdu;
    }

    /**
     * Creates a new Select File APDU.
     * @param fileID File identifier
     * @return Select File APDU
     */
    public static byte[] selectDF(byte[] fileID) {
        byte[] header = new byte[]{NULL, APDUConstants.SELECT_INS, (byte) 0x04, NULL};
        byte lc = (byte) (fileID.length & FF);

        return concatenate(header, lc, fileID);
    }

    /**
     * Creates a new Select File APDU.
     * @param fileID File identifier
     * @return Select File APDU
     */
    public static byte[] selectEF(short fileID) {
        byte[] header = new byte[]{NULL, APDUConstants.SELECT_INS, (byte) 0x02, (byte) 0x0C};
        byte[] data = ShortUtils.toByteArray(fileID);
        byte lc = (byte) 0x02;

        return concatenate(header, lc, data);
    }

    /**
     * Creates a new Select File APDU to select the master file 0x3F00.
     * @return Select File APDU
     */
    public static byte[] selectMF() {
        byte[] header = new byte[]{NULL, APDUConstants.SELECT_INS, NULL, (byte) 0x0C};
        byte[] data = new byte[]{(byte) 0x3F, NULL};
        byte lc = (byte) 0x02;

        return concatenate(header, lc, data);
    }

    /**
     * Creates a new Read Binary APDU.
     * @param offset Offset
     * @param length Expected Length
     * @return Read Binary APDU
     */
    public static byte[] readBinary(int offset, int length) {
        byte le = (byte) (length & FF);

        return readBinary(offset, le);
    }

    /**
     * Creates a new Read Binary APDU.
     * @param offset Offset
     * @param length Expected Length
     * @return Read Binary APDU
     */
    public static byte[] readBinary(int offset, byte length) {
        byte[] offsetPara = new byte[2];
        if (offset <= 0xFF) {
            offsetPara[1] = (byte) (offset & 0xFF);
        } else {
            offsetPara = IntegerUtils.toByteArray(offset);
        }

        return readBinary(offsetPara[0], offsetPara[1], length);
    }

    /**
     * Creates a new Read Binary APDU.
     * @param p1 First byte of offset
     * @param p2 Second byte of offset
     * @param le Expected Length
     * @return Read Binary APDU
     */
    public static byte[] readBinary(byte p1, byte p2, byte le) {
        byte[] apdu = {NULL, APDUConstants.READ_BINARY_INS, p1, p2, le};

        return apdu;
    }

    /**
     * Creates a new Reset Retry Counter APDU to unblock or change the PIN.
     * See TR-03110 2.05 Section B.11.9.
     * @param password Password
     * @param type Type
     * @return Reset Retry Counter APDU
     */
    public static byte[] resetRetryCounter(byte[] password, byte type) {
        byte[] header = new byte[4];
        header[0] = NULL;
        header[1] = APDUConstants.RESET_RETRY_COUNTER_INS;

        if (password != null) {
            header[2] = (byte) 0x02;
            header[3] = type;
            byte lc = (byte) (password.length & FF);

            return concatenate(header, lc, password);
        } else {
            header[2] = (byte) 0x03;
            header[3] = type;

            return header;
        }
    }

    /**
     * Creates a new PSO:VerifyCertificate APDU for Terminal Authentication.
     * @param content Certificate
     * @return PSO:VerifyCertificate APDU
     */
    public static byte[] psoVerifyCertificate(byte[] content) {
        byte[] header = new byte[]{NULL, APDUConstants.PSOVC_INS, NULL, (byte) 0xBE};

        if (content.length <= FF) {
            byte lc = (byte) (content.length & FF);

            return concatenate(header, lc, content);
        } else if (content.length > FF) {
            // Extended APDU
            byte[] lc = new byte[]{NULL, (byte) (content.length >>> 8), (byte) (content.length)};

            return concatenate(header, lc, content);
        }

        return null;
    }

    /**
     * Creates a new MSE:Set DST APDU for Terminal Authentication
     * TR-03110 2.05 Section B.11.4
     * @param chr Certificate Holder Reference
     * @return MSE:Set DST APDU
     */
    public static byte[] mseSetDST(byte[] chr) {
        byte[] header = new byte[]{NULL, APDUConstants.MSESet_DST_INS, (byte) 0x81, (byte) 0xB6};
        byte[] data = TLV.encode((byte) 0x83, chr);
        byte lc = (byte) (data.length & FF);

        return concatenate(header, lc, data);
    }

    /**
     * Creates a new MSE:Set AT APDU for PACE.
     * TR-03110 2.05 Section B.11.1.
     * @param psi PACESecurityInfos
     * @param chat Certificate Holder Authorization Template
     * @param type
     * @return MSE:Set AT APDU
     */
    public static byte[] mseSetAT(PACESecurityInfos psi, byte[] chat, byte type) {
        byte[] header = new byte[]{NULL, APDUConstants.MSESet_AT_INS, (byte) 0xC1, (byte) 0xA4};
        byte[] oid = ObjectIdentifierUtils.getValue(psi.getPACEInfo().getProtocol());
        byte[] data = TLV.encode((byte) 0x80, oid);
        data = ByteUtils.concatenate(data, TLV.encode((byte) 0x83, type));

        if (chat != null) {
            data = ByteUtils.concatenate(data, chat);
        }

        byte lc = (byte) (data.length & FF);

        return concatenate(header, lc, data);
    }

    /**
     * Creates a new MSE:Set AT APDU for Terminal Authentication.
     * TR-03110 2.05 Section B.11.1.
     * @param tsi TASecurityInfos
     * @param pkPCD Ephemeral Public Key PCD
     * @param chr Certificate Holder Reference
     * @param aad Auxiliary authenticated data
     * @return MSE:Set AT APDU
     */
    public static byte[] mseSetAT(TASecurityInfos tsi, byte[] chr, byte[] pkPCD, byte[] aad) {
        byte[] header = new byte[]{NULL, APDUConstants.MSESet_AT_INS, (byte) 0x81, (byte) 0xA4};

        // FIXME
//        LoggerFactory.getLogger().warn("Fixed TA oid!");
        byte[] oid = ObjectIdentifierUtils.getValue(TAObjectIdentifier.id_TA_ECDSA_SHA_256);
        byte[] data = TLV.encode((byte) 0x80, oid);

        if (chr != null) {
            data = ByteUtils.concatenate(data, TLV.encode((byte) 0x83, chr));
        }
        if (pkPCD != null) {
            data = ByteUtils.concatenate(data, TLV.encode((byte) 0x91, pkPCD));
        }
        if (aad != null) {
            data = ByteUtils.concatenate(data, aad);
        }

        byte lc = (byte) (data.length & FF);

        return concatenate(header, lc, data);
    }

    /**
     * Create a new MSE:Set AT APDU for Chip Authentication.
     * TR-03110 2.05 Section B.11.1.
     * @param csi CASecurityInfos
     * @param keyID Reference of a private key
     * @return MSE:Set AT APDU
     */
    public static byte[] mseSetAT(CASecurityInfos csi, byte[] keyID) {
        byte[] header = new byte[]{NULL, APDUConstants.MSESet_AT_INS, (byte) 0x41, (byte) 0xA4};

        byte[] oid = ObjectIdentifierUtils.getValue(CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128);
        byte[] data = TLV.encode((byte) 0x80, oid);

        if (keyID != null) {
            data = ByteUtils.concatenate(data, TLV.encode((byte) 0x84, ByteUtils.cutLeadingNullByte(keyID)));
        }

        byte lc = (byte) (data.length & FF);

        return concatenate(header, lc, data);
    }

    /**
     * Creates a new General Authenticate APDU.
     * TR-03110 2.05 Section B.11.2
     * APDU: 0x10 0x86 0x00 0x00 0x02 0x7C 0x00 0x00
     * @return General Authenticate APDU
     */
    public static byte[] generalAuthenticate() {
        byte[] header = new byte[]{(byte) 0x10, APDUConstants.GENERAL_AUTH_INS, NULL, NULL};
        byte[] data = new byte[]{APDUConstants.GENERAL_AUTH_DATA, NULL};
        byte lc = (byte) 0x02;

        return concatenate(header, lc, data, NULL);
    }

    /**
     * Creates a new General Authenticate APDU.
     * TR-03110 2.05 Section B.11.2
     * APDU: 0x10 0x86 0x00 0x00 LC 0x7C DATA 0x00
     * @param tag Protocol specific data tag. Tag of TLV encoded content.
     * @param content Data
     * @return General Authenticate APDU
     */
    public static byte[] generalAuthenticate(byte tag, byte[] content) {
        return generalAuthenticate((byte) 0x10, tag, content);
    }

    /**
     * Creates a new General Authenticate APDU.
     * TR-03110 2.05 Section B.11.2
     * APDU: 0x00 0x86 0x00 0x00 LC 0x7C DATA 0x00
     * @param prefix Protocol specific prefix
     * @param tag Protocol specific data tag. Tag of TLV encoded content.
     * @param content Content
     * @return General Authenticate APDU
     */
    public static byte[] generalAuthenticate(byte prefix, byte tag, byte[] content) {
        byte[] header = new byte[]{prefix, APDUConstants.GENERAL_AUTH_INS, NULL, NULL};
        byte[] data = TLV.encode(APDUConstants.GENERAL_AUTH_DATA, TLV.encode(tag, content));
        byte lc = (byte) (data.length & FF);

        return concatenate(header, lc, data, NULL);
    }

    /**
     * Creates a new External Authenticate APDU.
     * TR-03110 2.05 Section B.11.7
     * APDU: 0x00 0x82 0x00 0x00 LC DATA
     * @param content 
     * @return External Authenticate APDU
     */
    public static byte[] externalAuthentication(byte[] content) {
        byte[] header = new byte[]{NULL, APDUConstants.EXTERNAL_AUTH_INS, NULL, NULL};
        byte lc = (byte) (content.length & FF);

        return concatenate(header, lc, content);
    }

    public static byte[] concatenate(byte[] header, byte lc, byte[] data) {
        return concatenate(header, new byte[]{lc}, data);
    }

    public static byte[] concatenate(byte[] header, byte lc, byte[] data, byte le) {
        return concatenate(header, new byte[]{lc}, data, new byte[]{le});
    }

    public static byte[] concatenate(byte[] header, byte[] lc, byte[] data) {
        byte[] ret = new byte[header.length + lc.length + data.length];

        System.arraycopy(header, 0, ret, 0, header.length);
        System.arraycopy(lc, 0, ret, header.length, lc.length);
        System.arraycopy(data, 0, ret, header.length + lc.length, data.length);

        return ret;
    }

    public static byte[] concatenate(byte[] header, byte[] lc, byte[] data, byte[] le) {
        byte[] ret = new byte[header.length + lc.length + data.length + le.length];

        System.arraycopy(header, 0, ret, 0, header.length);
        System.arraycopy(lc, 0, ret, header.length, lc.length);
        System.arraycopy(data, 0, ret, header.length + lc.length, data.length);
        System.arraycopy(le, 0, ret, header.length + lc.length + data.length, le.length);

        return ret;
    }
}
