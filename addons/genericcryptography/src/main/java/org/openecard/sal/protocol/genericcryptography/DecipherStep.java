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

package org.openecard.sal.protocol.genericcryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.Decipher;
import iso.std.iso_iec._24727.tech.schema.DecipherResponse;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Map;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.common.ECardConstants;
import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.ManageSecurityEnvironment;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.anytype.CryptoMarkerType;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.common.tlv.TLV;
import org.openecard.common.util.ByteUtils;
import org.openecard.sal.protocol.genericcryptography.apdu.PSODecipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the Decipher step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.6.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DecipherStep implements ProtocolStep<Decipher, DecipherResponse> {

    private static final Logger logger = LoggerFactory.getLogger(DecipherStep.class);
    private static final byte PADDING_INDICATOR_BYTE = (byte) 0x00;
    private final Dispatcher dispatcher;

    /**
     * Creates a new DecipherStep.
     *
     * @param dispatcher Dispatcher
     */
    public DecipherStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.Decipher;
    }

    @Override
    public DecipherResponse perform(Decipher request, Map<String, Object> internalData) {
	DecipherResponse response = WSHelper.makeResponse(DecipherResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    String didName = SALUtils.getDIDName(request);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle);

	    Assert.securityConditionDID(cardStateEntry, applicationID, didName, CryptographicServiceActionName.DECIPHER);

	    DIDStructureType didStructure = SALUtils.getDIDStructure(request, didName, cardStateEntry, connectionHandle);
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didStructure.getDIDMarker());
	    byte[] keyReference = cryptoMarker.getCryptoKeyInfo().getKeyRef().getKeyRef();
	    byte[] algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getCardAlgRef();
	    byte[] slotHandle = connectionHandle.getSlotHandle();

	    // TODO eGK specific requirement
	    // See eGK specification, part 1, version 2.2.0, section 15.9.6.
	    if (didStructure.getDIDScope().equals(DIDScopeType.LOCAL)) {
		keyReference[0] = (byte) (0x80 | keyReference[0]);
	    }

	    TLV tagKeyReference = new TLV();
	    tagKeyReference.setTagNumWithClass(0x84);
	    tagKeyReference.setValue(keyReference);
	    TLV tagAlgorithmIdentifier = new TLV();
	    tagAlgorithmIdentifier.setTagNumWithClass(0x80);
	    tagAlgorithmIdentifier.setValue(algorithmIdentifier);
	    byte[] mseData = ByteUtils.concatenate(tagKeyReference.toBER(), tagAlgorithmIdentifier.toBER());

	    CardCommandAPDU apdu = new ManageSecurityEnvironment((byte) 0x41, ManageSecurityEnvironment.CT, mseData);
	    apdu.transmit(dispatcher, slotHandle);

	    byte[] ciphertext = request.getCipherText();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    BigInteger bitKeySize = cryptoMarker.getCryptoKeyInfo().getKeySize();
	    int blocksize = bitKeySize.divide(new BigInteger("8")).intValue();

	    // check if the ciphertext length is divisible by the blocksize without rest
	    if ((ciphertext.length % blocksize) != 0) {
		return WSHelper.makeResponse(DecipherResponse.class, WSHelper.makeResultError(
			ECardConstants.Minor.App.INCORRECT_PARM,
			"The length of the ciphertext should be a multiple of the blocksize."));
	    }

	    // decrypt the ciphertext block for block
	    for (int offset = 0; offset < ciphertext.length; offset += blocksize) {
		byte[] ciphertextblock = ByteUtils.copy(ciphertext, offset, blocksize);
		apdu = new PSODecipher(ByteUtils.concatenate(PADDING_INDICATOR_BYTE, ciphertextblock), (byte) blocksize);
		CardResponseAPDU responseAPDU = apdu.transmit(dispatcher, slotHandle);
		baos.write(responseAPDU.getData());
	    }

	    response.setPlainText(baos.toByteArray());
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

}
