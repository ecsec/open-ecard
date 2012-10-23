/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.sal.protocol.genericcryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.Decipher;
import iso.std.iso_iec._24727.tech.schema.DecipherResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import javax.smartcardio.ResponseAPDU;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the ProtocolStep interface for the Decipher step of the GenericCryptography protocol.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DecipherStep implements ProtocolStep<Decipher, DecipherResponse> {

    private static final Logger _logger = LoggerFactory.getLogger(DecipherStep.class);

    private Dispatcher dispatcher;

    /**
     * 
     * @param dispatcher
     *            the dispatcher to use for message delivery
     */
    public DecipherStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.Decipher;
    }

    private ResponseAPDU transmitSingleAPDU(byte[] apdu, byte[] slotHandle) throws WSException, IllegalAccessException,
	    NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	ArrayList<byte[]> responses = new ArrayList<byte[]>() {
	    {
		add(new byte[] { (byte) 0x90, (byte) 0x00 });
		add(new byte[] { (byte) 0x63, (byte) 0xC3 });
	    }
	};

	Transmit t = CardCommands.makeTransmit(slotHandle, apdu, responses);
	TransmitResponse tr = (TransmitResponse) WSHelper.checkResult((TransmitResponse) this.dispatcher.deliver(t));
	return new ResponseAPDU(tr.getOutputAPDU().get(0));
    }

    @Override
    public DecipherResponse perform(Decipher decipher, Map<String, Object> internalData) {
	DecipherResponse res = new DecipherResponse();
	try {
	    ConnectionHandleType connectionHandle = decipher.getConnectionHandle();
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    CardStateEntry cardStateEntry = (CardStateEntry) internalData.get("cardState");
	    String didName = decipher.getDIDName();
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName,
		    connectionHandle.getCardApplication());
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(
		    (iso.std.iso_iec._24727.tech.schema.CryptoMarkerType) didStructure.getDIDMarker());

	    if (!cardStateEntry.checkDIDSecurityCondition(connectionHandle.getCardApplication(), didName,
		    CryptographicServiceActionName.DECIPHER)) {
		return WSHelper.makeResponse(DecipherResponse.class,
			WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	    }

	    byte keyReference = cryptoMarker.getCryptoKeyInfo().getKeyRef().getKeyRef()[0];
	    byte algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getCardAlgRef()[0];

	    if (didStructure.getDIDScope().equals(DIDScopeType.LOCAL)) {
		keyReference = (byte) (0x80 | keyReference);
	    }

	    ResponseAPDU rapdu = transmitSingleAPDU(
		    CardCommands.ManageSecurityEnvironment.mseSelectPrKeyDecipher(keyReference, algorithmIdentifier),
		    slotHandle);
	    byte[] ciphertext = decipher.getCipherText();
	    ByteArrayOutputStream plaintext = new ByteArrayOutputStream();
	    BigInteger bitKeySize = cryptoMarker.getCryptoKeyInfo().getKeySize();
	    int blocksize = bitKeySize.divide(new BigInteger("8")).intValue();

	    // check if the ciphertext length is divisible by the blocksize without rest
	    if ((ciphertext.length % blocksize) != 0)
		return WSHelper.makeResponse(DecipherResponse.class, WSHelper.makeResultError(
			ECardConstants.Minor.App.INCORRECT_PARM,
			"The length of the ciphertext should be a multiple of the blocksize."));

	    // decrypt the ciphertext block for block
	    for (int offset = 0; offset < ciphertext.length; offset += blocksize) {
		byte[] ciphertextblock = ByteUtils.copy(ciphertext, offset, blocksize);
		rapdu = transmitSingleAPDU(
			CardCommands.PerformSecurityOperation.decipher(
				ByteUtils.concatenate((byte) 0x00, ciphertextblock), (short) blocksize), slotHandle);
		plaintext.write(rapdu.getData());
	    }

	    Result result = new Result();
	    result.setResultMajor(org.openecard.client.common.ECardConstants.Major.OK);
	    res.setResult(result);
	    res.setPlainText(plaintext.toByteArray());
	} catch (Exception e) {
	    _logger.warn(e.getMessage(), e);
	    res = WSHelper.makeResponse(DecipherResponse.class, WSHelper.makeResult(e));
	}
	return res;
    }

}
