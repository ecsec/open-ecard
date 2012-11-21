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

package org.openecard.common.util;

import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
@Deprecated
public class CardCommands {

    private static final byte CLASS_BYTE = (byte) 0x00;

    /**
     * Remove trailer (aka status bytes) from response APDU.
     *
     * @param responseApdu
     * @return
     */
    public static byte[] getDataFromResponse(byte[] responseApdu) {
	return Arrays.copyOf(responseApdu, responseApdu.length - 2);
    }

    /**
     * Remove data from response APDU thereby only returning trailer (aka status
     * bytes).
     *
     * @param responseApdu
     * @return
     */
    public static byte[] getResultFromResponse(byte[] responseApdu) {
	return Arrays.copyOfRange(responseApdu, responseApdu.length - 2, responseApdu.length);
    }

    public static InputAPDUInfoType makeApdu(byte[] cmd, List<byte[]> responses) {
	InputAPDUInfoType apdu = new InputAPDUInfoType();
	apdu.setInputAPDU(cmd);
	apdu.getAcceptableStatusCode().addAll(responses);
	return apdu;
    }

    public static Transmit makeTransmit(byte[] slotHandle, byte[] cmd, List<byte[]> responses) {
	Transmit t = new Transmit();
	t.setSlotHandle(slotHandle);
	InputAPDUInfoType apdu = makeApdu(cmd, responses);
	t.getInputAPDUInfo().add(apdu);
	return t;
    }

    private static byte[] buildContent(byte[] content) {
	// create lc and make it extended if needed
	byte[] lc = IntegerUtils.toByteArray(content.length);
	if (lc.length == 2) {
	    lc = ByteUtils.concatenate((byte) 0, lc);
	}
	content = ByteUtils.concatenate(lc, content);
	return content;
    }

    private static byte[] buildLength(short length, byte[] content) {
	// Le with value 0x00 means maximum length expected
	if (length == (short) 0) {
	    return new byte[] { 0 };
	}
	// only care about Lc field if there is a content
	byte[] data;
	if (content == null && length > (short) 0xFF) {
	    data = new byte[] { 0 };
	} else {
	    data = new byte[0];
	}
	if (length > 0xFF) { // Lc + extended length
	    data = ByteUtils.concatenate(data, ShortUtils.toByteArray(length));
	    return data;
	} else { // Lc + short length
	    data = ByteUtils.concatenate(data, (byte) length);
	    return data;
	}
    }

    public static byte[] genericCommand(byte cmd, byte[] p12, byte[] content) {
	byte[] apdu = { CLASS_BYTE, cmd };
	apdu = ByteUtils.concatenate(apdu, p12);
	content = buildContent(content);
	apdu = ByteUtils.concatenate(apdu, content);
	return apdu;
    }

    public static byte[] genericCommand(byte cmd, byte[] p12, byte[] content, short length) {
	byte[] apdu = { CLASS_BYTE, cmd };
	apdu = ByteUtils.concatenate(apdu, p12);
	content = buildContent(content);
	apdu = ByteUtils.concatenate(apdu, content);
	byte[] lenBytes = buildLength(length, content);
	apdu = ByteUtils.concatenate(apdu, lenBytes);
	return apdu;
    }

    public static byte[] genericCommand(byte cmd, byte[] p12) {
	byte[] apdu = { CLASS_BYTE, cmd };
	apdu = ByteUtils.concatenate(apdu, p12);
	return apdu;
    }

    public static byte[] genericCommand(byte cmd, byte[] p12, short length) {
	byte[] apdu = { CLASS_BYTE, cmd };
	apdu = ByteUtils.concatenate(apdu, p12);
	byte[] lenBytes = buildLength(length, null);
	apdu = ByteUtils.concatenate(apdu, lenBytes);
	return apdu;
    }

    public static byte[] addCommandChaining(byte[] apdu) {
	apdu[0] |= 0x10;
	return apdu;
    }

    public static class InternalAuthenticate {
	private static final byte COMMAND_INTERNAL_AUTHENTICATE = (byte) 0x88;

	/**
	 * Creates an APDU that is equivalent to the command
	 * "INTERNAL AUTHENTICATE". The created APDU is a case 4 short or
	 * extended command APDU.
	 *
	 * @param data
	 *            - data which shall be authenticated
	 * @param le
	 *            - expected length of answer
	 * @return APDU as byte array
	 */
	public static byte[] generic(byte[] data, short le) {
	    return genericCommand(COMMAND_INTERNAL_AUTHENTICATE, new byte[] { 0x00, 0x00 }, data, le);
	}
    }

    public static class Verify {
	private static final byte COMMAND_VERIFY = (byte) 0x20;

	/**
	 * Creates an APDU that is equivalent to the command "VERIFY". The
	 * created APDU is a case 3 short command APDU.
	 *
	 * @param p1
	 *            - parameter for data field
	 * @param p2
	 *            - password reference
	 * @param data
	 *            - data to be verified
	 * @return APDU as byte array
	 */
	public static byte[] generic(byte p1, byte p2, byte[] data) {
	    return genericCommand(COMMAND_VERIFY, new byte[] { p1, p2 }, data);
	}

    }

    public static class GetChallenge {
	private static final byte COMMAND_GET_CHALLENGE = (byte) 0x84;

	/**
	 * Creates an APDU that is equivalent to the command "GET CHALLENGE".
	 * The requested challenge will have a size of 8 bytes. The created APDU
	 * is a case 2 short command APDU.
	 *
	 * @return APDU as byte array
	 */
	public static byte[] generic() {
	    return genericCommand(COMMAND_GET_CHALLENGE, new byte[] { 0x00, 0x00 }, (short) 0x08);
	}

	/**
	 * Creates an APDU that is equivalent to the command "GET CHALLENGE".
	 * The created APDU is a case 2 short command APDU.
	 *
	 * @param le
	 *            - size of requested challenge
	 * @return APDU as byte array
	 */
	public static byte[] generic(short le) {
	    return genericCommand(COMMAND_GET_CHALLENGE, new byte[] { 0x00, 0x00 }, le);
	}

    }

    public static class ExternalAuthenticate {
	private static final byte COMMAND_EXTERNAL_AUTHENTICATE = (byte) 0x82;

	/**
	 * Creates an APDU that is equivalent to the command
	 * "EXTERNAL AUTHENTICATE". Depending on the size of the parameter data,
	 * the created APDU is either a case 3 short command APDU or a case 3
	 * extended command APDU.
	 *
	 * @param data
	 *            - data which originates from an external entity (e.g.
	 *            another smart card)
	 * @return APDU as byte array
	 */
	public static byte[] generic(byte[] data) {
	    byte[] p12 = new byte[] { (byte) 0x00, (byte) 0x00 };
	    return genericCommand(COMMAND_EXTERNAL_AUTHENTICATE, p12, data);
	}

    }

    public static class ManageSecurityEnvironment {
	private static final byte COMMAND_MANAGE_SECURITY_ENVIRONMENT = (byte) 0x22;

	/**
	 * Creates an APDU that is equivalent to the command
	 * "MSE Set DST". Depending on the size of the parameter data,
	 * the created APDU is either a case 3 short command APDU or a case 3
	 * extended command APDU.
	 *
	 * @param data
	 *            - data which originates from an external entity (e.g.
	 *            another smart card)
	 * @return APDU as byte array
	 */
	public static byte[] setDST(byte[] data) {
	    byte[] p12 = new byte[] { (byte) 0x81, (byte) 0xB6 };
	    data = ByteUtils.concatenate(new byte[] { (byte) 0x83, (byte) data.length },
		    data);
	    return genericCommand(COMMAND_MANAGE_SECURITY_ENVIRONMENT, p12, data);
	}

	public static class setAT {

	    /**
	     * Creates a new MSE:Set AT APDU for PACE. TR-03110 2.05 Section
	     * B.11.1.
	     *
	     * @param oid
	     *            Cryptograhic mechanism reference
	     * @param chat
	     *            Certificate Holder Authorization Template
	     * @param passwordType type of the password
	     * @return MSE:Set AT APDU
	     */
	    public static byte[] PACE(byte[] oid, byte passwordType, byte[] chat) {
		byte[] p12 = new byte[] { (byte) 0xC1, (byte) 0xA4 };

		byte[] data = ByteUtils.concatenate(new byte[] { (byte) 0x80, (byte) oid.length }, oid);
		data = ByteUtils.concatenate(data, new byte[] { (byte) 0x83, 0x01, passwordType });

		if (chat != null) {
		    data = ByteUtils.concatenate(data, chat);
		}

		return genericCommand(COMMAND_MANAGE_SECURITY_ENVIRONMENT, p12, data);
	    }

	    /**
	     * Creates a new MSE:Set AT APDU for Terminal Authentication.
	     * TR-03110 2.05 Section B.11.1.
	     *
	     * @param oid
	     *            Cryptograhic mechanism reference
	     * @param pkPCD
	     *            Ephemeral Public Key PCD
	     * @param chr
	     *            Certificate Holder Reference
	     * @param aad
	     *            Auxiliary authenticated data
	     * @return MSE:Set AT APDU
	     */
	    public static byte[] TA(byte[] oid, byte[] chr, byte[] pkPCD, byte[] aad) {
		byte[] p12 = new byte[] { (byte) 0x81, (byte) 0xA4 };

		byte[] data = ByteUtils.concatenate(new byte[] { (byte) 0x80, (byte) oid.length }, oid);

		if (chr != null) {
		    data = ByteUtils.concatenate(data, ByteUtils.concatenate(new byte[] { (byte) 0x83, (byte) chr.length }, chr));
		}
		if (pkPCD != null) {
		    data = ByteUtils.concatenate(data, ByteUtils.concatenate(new byte[] { (byte) 0x91, (byte) pkPCD.length }, pkPCD));
		}
		if (aad != null) {
		    data = ByteUtils.concatenate(data, aad);
		}

		return genericCommand(COMMAND_MANAGE_SECURITY_ENVIRONMENT, p12, data);
	    }

	    /**
	     * Create a new MSE:Set AT APDU for Chip Authentication. TR-03110
	     * 2.05 Section B.11.1.
	     *
	     * @param oid
	     *            Cryptograhic mechanism reference
	     * @param keyID
	     *            Reference of a private key
	     * @return MSE:Set AT APDU
	     */
	    public static byte[] CA(byte[] oid, byte[] keyID) {
		byte[] p12 = new byte[] { (byte) 0x41, (byte) 0xA4 };
		byte[] data = ByteUtils.concatenate(new byte[] { (byte) 0x80, (byte) oid.length }, oid);
		if (keyID != null) {
		    keyID = ByteUtils.cutLeadingNullByte(keyID);
		    data = ByteUtils.concatenate(data, ByteUtils.concatenate(new byte[] { (byte) 0x84, (byte) keyID.length }, keyID));
		}

		return genericCommand(COMMAND_MANAGE_SECURITY_ENVIRONMENT, p12, data);
	    }

	}

	/**
	 * Creates an APDU that is equivalent to the command
	 * "MANAGE SECURITY ENVIRONMENT". The command selects a private key for
	 * a following "INTERNAL AUTHENTICATE". The created APDU is a case 3
	 * short command APDU.
	 *
	 * @param keyRef
	 *            - key reference
	 * @param algId
	 *            - algorithm identifier
	 * @return APDU as byte array
	 */
	@Deprecated
	public static byte[] mseSelectPrKeyIntAuth(byte keyRef, byte algId) {
	    byte[] p12 = new byte[] { (byte) 0x41, (byte) 0xA4 };
	    byte[] data = new byte[] { (byte) 0x84, (byte) 0x01, keyRef, (byte) 0x80, (byte) 0x01, algId };
	    return genericCommand(COMMAND_MANAGE_SECURITY_ENVIRONMENT, p12, data);
	}

	@Deprecated
	public static byte[] mseSelectPrKeySignature(byte keyRef, byte algId) {
	    byte[] p12 = new byte[] { (byte) 0x41, (byte) 0xB6 };
	    byte[] data = new byte[] { (byte) 0x84, (byte) 0x01, keyRef, (byte) 0x80, (byte) 0x01, algId };
	    return genericCommand(COMMAND_MANAGE_SECURITY_ENVIRONMENT, p12, data);
	}

	@Deprecated
	public static byte[] mseSelectPrKeyDecipher(byte keyRef, byte algId){
	    byte[] p12 = new byte[] { (byte) 0x41, (byte) 0xB8 };
	    byte[] data = new byte[] { (byte) 0x84, (byte) 0x01, keyRef, (byte) 0x80, (byte) 0x01, algId };
	    return genericCommand(COMMAND_MANAGE_SECURITY_ENVIRONMENT, p12, data);
	}

	/**
	 * Creates an APDU that is equivalent to the command
	 * "MANAGE SECURITY ENVIRONMENT". The command selects a public key for
	 * the verification of a card verifiable certificate (CVC). The created
	 * APDU is a case 3 short command APDU.
	 *
	 * @param keyRef
	 *            - key reference
	 * @return APDU as byte array
	 */
	@Deprecated
	public static byte[] mseSelectPubKeyCertVerification(byte[] keyRef) {
	    byte[] p12 = new byte[] { (byte) 0x81, (byte) 0xB6 };
	    byte[] data = ByteUtils.concatenate(new byte[] { (byte) 0x83, (byte) keyRef.length }, keyRef);
	    return genericCommand(COMMAND_MANAGE_SECURITY_ENVIRONMENT, p12, data);
	}

	/**
	 * Creates an APDU that is equivalent to the command
	 * "MANAGE SECURITY ENVIRONMENT". The command selects a public key for a
	 * following "EXTERNAL AUTHENTICATE". The created APDU is a case 3 short
	 * command APDU.
	 *
	 * @param keyRef
	 *            - key reference
	 * @param algId
	 *            - algorithm identifier
	 * @return APDU as byte array
	 */
	@Deprecated
	public static byte[] mseSelectPubKeyExtAuth(byte[] keyRef, byte algId) {
	    byte[] p12 = new byte[] { (byte) 0x81, (byte) 0xA4 };
	    byte[] data = ByteUtils.concatenate(ByteUtils.concatenate(new byte[] { (byte) 0x83, (byte) keyRef.length }, keyRef),
		    new byte[] { (byte) 0x80, (byte) 0x01, algId });
	    return genericCommand(COMMAND_MANAGE_SECURITY_ENVIRONMENT, p12, data);
	}

    }

    public static class GeneralAuthenticate {

	private static final byte COMMAND_GENERAL_AUTHENTICATE = (byte) 0x86;
	private static final byte GENERAL_AUTHENTICATION_DATA_TAG = (byte) 0x7C;
	private static final byte[] p12 = new byte[] { (byte) 0x00, (byte) 0x00 };

	/**
	 * Creates a new General Authenticate APDU. TR-03110 2.05 Section B.11.2
	 * APDU: 0x10 0x86 0x00 0x00 0x02 0x7C 0x00 0x00
	 *
	 * @return General Authenticate APDU
	 */
	public static byte[] getNonce() {
	    byte[] command = genericCommand(COMMAND_GENERAL_AUTHENTICATE, p12, new byte[] { GENERAL_AUTHENTICATION_DATA_TAG, (byte) 0x00 },
		    (short) 0x00);
	    return addCommandChaining(command);
	}

	public static byte[] generic(byte tag, byte[] content, boolean commandChaining) {
	    byte[] mappingData = ByteUtils.concatenate(new byte[] { tag, (byte) content.length }, content);
	    byte[] data = ByteUtils.concatenate(new byte[] { GENERAL_AUTHENTICATION_DATA_TAG, (byte) mappingData.length }, mappingData);
	    byte[] command = genericCommand(COMMAND_GENERAL_AUTHENTICATE, p12, data, (short) 0x00);
	    if (commandChaining) {
		return addCommandChaining(command);
	    }
	    else {
		return command;
	    }
	}

    }

    public static class PerformSecurityOperation {

	private static final byte COMMAND_PERFORM_SECURITY_OPERATION = (byte) 0x2A;
	private static final byte VERIFY_SELF_DESCRIPTIVE_CERTIFICATE = (byte) 0xBE;
	private static final byte VERIFY_NOT_SELF_DESCRIPTIVE_CERTIFICATE = (byte) 0xAE;

	/**
	 * Creates an APDU that is equivalent to the command
	 * "PERFORM SECURITY OPERATION". Depending on the size of the parameter
	 * data, the created APDU is either a case 3 short command APDU or a
	 * case 3 extended command APDU. The command verifies a card verifiable
	 * certificate (CVC).
	 *
	 * @param certificate
	 *            - CVC to be verified
	 * @return APDU as byte array
	 */
	public static byte[] verifySelfDescriptiveCertificate(byte[] certificate) {
	    byte[] verifySelfCert = new byte[] { 0x00, VERIFY_SELF_DESCRIPTIVE_CERTIFICATE };
	    return genericCommand(COMMAND_PERFORM_SECURITY_OPERATION, verifySelfCert, certificate);
	}

	public static byte[] verifyNotSelfDescriptiveCertificate(byte[] certificate) {
	    byte[] verifyNotSelfCert = new byte[] { 0x00, VERIFY_NOT_SELF_DESCRIPTIVE_CERTIFICATE };
	    return genericCommand(COMMAND_PERFORM_SECURITY_OPERATION, verifyNotSelfCert, certificate);
	}

	public static byte[] computeDigitalSignature(byte[] input) {
	    byte[] p12 = new byte[]{(byte)0x9E,(byte) 0x9A};
	    return genericCommand(COMMAND_PERFORM_SECURITY_OPERATION, p12, input, (short) 0x00);
	}

	public static byte[] decipher(byte[] input, short length) {
	    byte[] p12 = new byte[]{(byte)0x80,(byte) 0x86};
	    return genericCommand(COMMAND_PERFORM_SECURITY_OPERATION, p12, input, length);
	}

	// TODO add further pso commands

    }

    public static class Read {

	private static final byte COMMAND_READ_BINARY1 = (byte) 0xB0;
	private static final byte COMMAND_READ_BINARY2 = (byte) 0xB1;
	private static final byte COMMAND_READ_RECORD1 = (byte) 0xB2;
	private static final byte COMMAND_READ_RECORD2 = (byte) 0xB3;

	public static List<byte[]> positiveResponses() {
	    return new ArrayList<byte[]>() {
		{
		    add(new byte[] { (byte) 0x90, (byte) 0x00 });
		    add(new byte[] { (byte) 0x62, (byte) 0x82 });
		}
	    };
	}

	public static Transmit makeTransmit(byte[] slotHandle, byte[] cmd) {
	    return CardCommands.makeTransmit(slotHandle, cmd, positiveResponses());
	}

	private static byte[] genericBinary(byte cmd, byte[] p12, byte[] content, short length) {
	    // fix p12 if one byte missing
	    if (p12.length == 1) {
		p12 = ByteUtils.concatenate((byte) 0x00, p12);
	    }
	    byte[] apdu;
	    if (content == null) {
		apdu = genericCommand(cmd, p12, length);
	    } else {
		apdu = genericCommand(cmd, p12, content, length);
	    }
	    return apdu;
	}

	public static byte[] binaryWithShortId(byte shortId, short offset, short len) {
	    if (offset > 0xFF) {
		byte[] dataBytes = ShortUtils.toByteArray(offset);
		// write length tag
		dataBytes = ByteUtils.concatenate((byte) dataBytes.length, dataBytes);
		dataBytes = ByteUtils.concatenate((byte) 0x54, dataBytes);
		// write discretionary data tag
		dataBytes = ByteUtils.concatenate((byte) dataBytes.length, dataBytes);
		dataBytes = ByteUtils.concatenate((byte) 0x53, dataBytes);
		return genericBinary(COMMAND_READ_BINARY2, new byte[] { 0, shortId }, dataBytes, len);
	    } else {
		byte p1 = (byte) ((0x1F & shortId) | 0x80); // first bit set and
							    // 5 to 1 is id
		return genericBinary(COMMAND_READ_BINARY1, new byte[] { p1, (byte) offset }, null, len);
	    }
	}

	public static byte[] binaryWithShortId(byte shortId, short offset) {
	    return binaryWithShortId(shortId, offset, (short) 0xFF);
	}

	public static byte[] binary(short offset, short len) {
	    byte[] p12 = ShortUtils.toByteArray(offset);
	    if (p12.length == 2) {
		p12[0] &= 0x7F; // make sure bit 8 is 0
	    }
	    return genericBinary(COMMAND_READ_BINARY1, p12, null, len);
	}

	public static byte[] binary(short offset) {
	    return binary(offset, (short) 0xFF);
	}

	public static byte[] binary() {
	    return binary((short) 0x00);
	}

	public static byte[] binaryWithExtraOffset(short fileId, short offset, short len) {
	    byte[] p12 = ShortUtils.toByteArray(fileId);
	    // build offset tag structure
	    byte[] content = ShortUtils.toByteArray(offset);
	    content = ByteUtils.concatenate((byte) content.length, content);
	    content = ByteUtils.concatenate((byte) 0x54, content); // returns
								   // tag 0x53
								   // or 0x73
	    return genericBinary(COMMAND_READ_BINARY2, p12, content, len);
	}

	public static byte[] binaryWithExtraOffset(short fileId, short offset) {
	    return binaryWithExtraOffset(fileId, offset, (short) 0xFF);
	}

	private static byte[] genericRecord(byte shortId, byte readType, byte record, short length) {
	    byte p1 = record;
	    byte p2 = (byte) (shortId << 3);
	    p2 |= readType & 0x07;
	    byte[] apdu = genericCommand(COMMAND_READ_RECORD1, new byte[] { p1, p2 }, length);
	    return apdu;
	}

	private static byte[] genericRecord(byte shortId, byte readType, byte record, short offset, short length) {
	    byte p1 = record;
	    byte p2 = (byte) (shortId << 3);
	    p2 |= readType & 0x07;
	    byte[] content = ShortUtils.toByteArray(offset);
	    content = ByteUtils.concatenate((byte) content.length, content);
	    content = ByteUtils.concatenate((byte) 0x54, content);
	    byte[] apdu = genericCommand(COMMAND_READ_RECORD1, new byte[] { p1, p2 }, length);
	    return apdu;
	}

	public static byte[] recordNumber(byte number) {
	    return genericRecord((byte) 0x00, (byte) 0x04, number, (short) 0xFF);
	}

	public static byte[] recordNumber_Ext(byte number) {
	    return genericRecord((byte) 0x00, (byte) 0x04, number, (short) 0xFFFF);
	}

	public static byte[] recordNumberWithShortId(byte shortId, byte number) {
	    return genericRecord(shortId, (byte) 0x04, number, (short) 0xFF);
	}

	public static byte[] recordNumberWithShortId_Ext(byte shortId, byte number) {
	    return genericRecord(shortId, (byte) 0x04, number, (short) 0xFFFF);
	}

	public static byte[] recordsFromNumber(byte number) {
	    return genericRecord((byte) 0x00, (byte) 0x05, number, (short) 0xFF);
	}

	public static byte[] recordsNumber_Ext(byte number) {
	    return genericRecord((byte) 0x00, (byte) 0x05, number, (short) 0xFFFF);
	}

	// TODO: add other combinations

    }

    public static class Select {

	private static final byte COMMAND_SELECT = (byte) 0xA4;

	public static List<byte[]> positiveResponses() {
	    return new ArrayList<byte[]>() {
		{
		    add(new byte[] { (byte) 0x90, (byte) 0x00 });
		}
	    };
	}

	public static Transmit makeTransmit(byte[] slotHandle, byte[] cmd) {
	    return CardCommands.makeTransmit(slotHandle, cmd, positiveResponses());
	}

	private static byte[] generic(byte[] fid, byte p1, byte p2, short length) {
	    byte[] p12 = new byte[] { p1, p2 };
	    byte[] apdu;
	    if ((p2 & 0x0C) != 0x0C) { // check file control information for
				       // response data
		if (fid == null) {
		    apdu = genericCommand(COMMAND_SELECT, p12, length);
		} else {
		    apdu = genericCommand(COMMAND_SELECT, p12, fid, length);
		}
	    } else {
		if (fid == null) {
		    apdu = genericCommand(COMMAND_SELECT, p12);
		} else {
		    apdu = genericCommand(COMMAND_SELECT, p12, fid);
		}
	    }
	    return apdu;
	}

	private static byte[] generic(byte[] fid, byte p1, byte p2) {
	    return generic(fid, p1, p2, (short) 0xFF);
	}

	private static byte[] generic(byte p1, byte p2) {
	    return generic(null, p1, p2);
	}

	/**
	 * Creates an APDU that is equivalent to the command
	 * "SELECT APPLICATION". The created APDU is a case 3 short command
	 * APDU.
	 *
	 * @param aid
	 *            - application identifier
	 * @return APDU as byte array
	 */
	public static byte[] application(byte[] aid) {
	    return application(aid, (byte) 0x0C);
	}

	public static byte[] application_FCI(byte[] aid) {
	    return application(aid, (byte) 0x00);
	}

	public static byte[] application_FCP(byte[] aid) {
	    return application(aid, (byte) 0x04);
	}

	public static byte[] application_FMD(byte[] aid) {
	    return application(aid, (byte) 0x08);
	}

	public static byte[] application_FCI_Ext(byte[] aid) {
	    return application(aid, (byte) 0x00, (short) 0xFFFF);
	}

	public static byte[] application_FCP_Ext(byte[] aid) {
	    return application(aid, (byte) 0x04, (short) 0xFFFF);
	}

	public static byte[] application_FMD_Ext(byte[] aid) {
	    return application(aid, (byte) 0x08, (short) 0xFFFF);
	}

	private static byte[] application(byte[] aid, byte p2) {
	    return generic(aid, (byte) 0x04, p2);
	}

	private static byte[] application(byte[] aid, byte p2, short length) {
	    return generic(aid, (byte) 0x04, p2, length);
	}

	/**
	 * Creates an APDU that is equivalent to the command "SELECT DIRECTORY".
	 * The created APDU is a case 3 short command APDU.
	 *
	 * @param fid
	 *            - file identifier for directory
	 * @return APDU as byte array
	 */
	public static byte[] DF(byte[] fid) {
	    return DF(fid, (byte) 0x0C);
	}

	public static byte[] DF_FCI(byte[] fid) {
	    return DF(fid, (byte) 0x00);
	}

	public static byte[] DF_FCP(byte[] fid) {
	    return DF(fid, (byte) 0x04);
	}

	public static byte[] DF_FMD(byte[] fid) {
	    return DF(fid, (byte) 0x08);
	}

	public static byte[] DF_FCI_Ext(byte[] fid) {
	    return DF(fid, (byte) 0x00, (short) 0xFFFF);
	}

	public static byte[] DF_FCP_Ext(byte[] fid) {
	    return DF(fid, (byte) 0x04, (short) 0xFFFF);
	}

	public static byte[] DF_FMD_Ext(byte[] fid) {
	    return DF(fid, (byte) 0x08, (short) 0xFFFF);
	}

	private static byte[] DF(byte[] fid, byte p2) {
	    return generic(fid, (byte) 0x01, p2);
	}

	private static byte[] DF(byte[] fid, byte p2, short length) {
	    return generic(fid, (byte) 0x01, p2, length);
	}

	public static byte[] parent() {
	    return parent((byte) 0x0C);
	}

	public static byte[] parent_FCI() {
	    return parent((byte) 0x00);
	}

	public static byte[] parent_FCP() {
	    return parent((byte) 0x04);
	}

	public static byte[] parent_FMD() {
	    return parent((byte) 0x08);
	}

	public static byte[] parent_FCI_Ext() {
	    return parent((byte) 0x00, (short) 0xFFFF);
	}

	public static byte[] parent_FCP_Ext() {
	    return parent((byte) 0x04, (short) 0xFFFF);
	}

	public static byte[] parent_FMD_Ext() {
	    return parent((byte) 0x08, (short) 0xFFFF);
	}

	private static byte[] parent(byte p2) {
	    return generic((byte) 0x03, p2);
	}

	private static byte[] parent(byte p2, short length) {
	    return generic(null, (byte) 0x03, p2, length);
	}

	/**
	 * Creates an APDU that is equivalent to the command "SELECT FILE". The
	 * created APDU is a case 3 short command APDU.
	 *
	 * @param fid
	 *            - file identifier
	 * @return APDU as byte array
	 */
	public static byte[] EF(byte[] fid) {
	    return EF(fid, (byte) 0x0C);
	}

	public static byte[] EF_FCI(byte[] fid) {
	    return EF(fid, (byte) 0x00);
	}

	public static byte[] EF_FCP(byte[] fid) {
	    return EF(fid, (byte) 0x04);
	}

	public static byte[] EF_FMD(byte[] fid) {
	    return EF(fid, (byte) 0x08);
	}

	public static byte[] EF_FCI_Ext(byte[] fid) {
	    return EF(fid, (byte) 0x00, (short) 0xFFFF);
	}

	public static byte[] EF_FCP_Ext(byte[] fid) {
	    return EF(fid, (byte) 0x04, (short) 0xFFFF);
	}

	public static byte[] EF_FMD_Ext(byte[] fid) {
	    return EF(fid, (byte) 0x08, (short) 0xFFFF);
	}

	private static byte[] EF(byte[] fid, byte p2) {
	    return generic(fid, (byte) 0x02, p2);
	}

	private static byte[] EF(byte[] fid, byte p2, short length) {
	    return generic(fid, (byte) 0x02, p2, length);
	}

	public static byte[] absolutePath(byte[] path) {
	    return absolutePath(path, (byte) 0x0C);
	}

	public static byte[] absolutePath_FCI(byte[] path) {
	    return absolutePath(path, (byte) 0x00);
	}

	public static byte[] absolutePath_FCP(byte[] path) {
	    return absolutePath(path, (byte) 0x04);
	}

	public static byte[] absolutePath_FMD(byte[] path) {
	    return absolutePath(path, (byte) 0x08);
	}

	public static byte[] absolutePath_Ext(byte[] path) {
	    return absolutePath(path, (byte) 0x0C, (short) 0xFFFF);
	}

	public static byte[] absolutePath_FCI_Ext(byte[] path) {
	    return absolutePath(path, (byte) 0x00, (short) 0xFFFF);
	}

	public static byte[] absolutePath_FCP_Ext(byte[] path) {
	    return absolutePath(path, (byte) 0x04, (short) 0xFFFF);
	}

	public static byte[] absolutePath_FMD_Ext(byte[] path) {
	    return absolutePath(path, (byte) 0x08, (short) 0xFFFF);
	}

	private static byte[] absolutePath(byte[] path, byte p2) {
	    return generic(path, (byte) 0x08, p2);
	}

	private static byte[] absolutePath(byte[] path, byte p2, short length) {
	    return generic(path, (byte) 0x08, p2, length);
	}

	public static byte[] relativePath(byte[] path) {
	    return relativePath(path, (byte) 0x0C);
	}

	public static byte[] relativePath_FCI(byte[] path) {
	    return relativePath(path, (byte) 0x00);
	}

	public static byte[] relativePath_FCP(byte[] path) {
	    return relativePath(path, (byte) 0x04);
	}

	public static byte[] relativePath_FMD(byte[] path) {
	    return relativePath(path, (byte) 0x08);
	}

	public static byte[] relativePath_Ext(byte[] path) {
	    return relativePath(path, (byte) 0x0C, (short) 0xFFFF);
	}

	public static byte[] relativePath_FCI_Ext(byte[] path) {
	    return relativePath(path, (byte) 0x00, (short) 0xFFFF);
	}

	public static byte[] relativePath_FCP_Ext(byte[] path) {
	    return relativePath(path, (byte) 0x04, (short) 0xFFFF);
	}

	public static byte[] relativePath_FMD_Ext(byte[] path) {
	    return relativePath(path, (byte) 0x08, (short) 0xFFFF);
	}

	private static byte[] relativePath(byte[] path, byte p2) {
	    return generic(path, (byte) 0x09, p2);
	}

	private static byte[] relativePath(byte[] path, byte p2, short length) {
	    return generic(path, (byte) 0x09, p2, length);
	}

	private static final byte[] MF_fid = new byte[] { (byte) 0x3F, (byte) 0x00 };

	/**
	 * Creates an APDU that selects the MASTER FILE. The created APDU is a
	 * case 1 command APDU.
	 *
	 * @return APDU as byte array
	 */
	public static byte[] MF() {
	    return MF((byte) 0x0C);
	}

	public static byte[] MF_FCI() {
	    return MF((byte) 0x00);
	}

	public static byte[] MF_FCP() {
	    return MF((byte) 0x04);
	}

	public static byte[] MF_FMD() {
	    return MF((byte) 0x08);
	}

	public static byte[] MF_FCI_Ext() {
	    return MF((byte) 0x00, (short) 0xFFFF);
	}

	public static byte[] MF_FCP_Ext() {
	    return MF((byte) 0x04, (short) 0xFFFF);
	}

	public static byte[] MF_FMD_Ext() {
	    return MF((byte) 0x08, (short) 0xFFFF);
	}

	private static byte[] MF(byte p2) {
	    return generic(MF_fid, (byte) 0x00, p2);
	}

	private static byte[] MF(byte p2, short length) {
	    return generic(MF_fid, (byte) 0x00, p2, length);
	}

    }

}
