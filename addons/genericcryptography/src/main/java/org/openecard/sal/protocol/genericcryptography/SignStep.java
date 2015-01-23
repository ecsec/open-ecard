/****************************************************************************
 * Copyright (C) 2012-2015 HS Coburg.
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

import iso.std.iso_iec._24727.tech.schema.CardCallTemplateType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.bouncycastle.util.Arrays;
import org.openecard.common.ECardConstants;
import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.GetResponse;
import org.openecard.common.apdu.InternalAuthenticate;
import org.openecard.common.apdu.ManageSecurityEnvironment;
import org.openecard.common.apdu.common.APDUTemplateException;
import org.openecard.common.apdu.common.BaseTemplateContext;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardCommandTemplate;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.common.TLVFunction;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.Assert;
import org.openecard.crypto.common.sal.CryptoMarkerType;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.util.ByteUtils;
import org.openecard.sal.protocol.genericcryptography.apdu.PSOComputeDigitalSignature;
import org.openecard.sal.protocol.genericcryptography.apdu.PSOHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the Sign step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.9.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class SignStep implements ProtocolStep<Sign, SignResponse> {

    private static final Logger logger = LoggerFactory.getLogger(SignStep.class);

    //TODO extract the blocksize from somewhere
    private static final byte BLOCKSIZE = (byte) 256;
    private static final byte SET_COMPUTATION = (byte) 0x41;
    private static final byte KEY_REFERENCE_PRIVATE_KEY = (byte) 0x84;
    private static final byte CARD_ALG_REF = (byte) 0x80;

    private static final String HASHTOSIGN = "hashToSign";
    private static final String KEYREFERENCE = "keyReference";
    private static final String ALGORITHMIDENTIFIER = "algorithmIdentifier";
    private static final String HASHALGORITHMREFERENCE = "hashAlgorithmReference";

    private final Dispatcher dispatcher;

    /**
     * Creates a new SignStep.
     *
     * @param dispatcher Dispatcher
     */
    public SignStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.Sign;
    }

    @Override
    public SignResponse perform(Sign sign, Map<String, Object> internalData) {
	SignResponse response = WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(sign);
	    String didName = SALUtils.getDIDName(sign);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle);
	    DIDStructureType didStructure = SALUtils.getDIDStructure(sign, didName, cardStateEntry, connectionHandle);
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didStructure.getDIDMarker());

	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    byte[] applicationID = connectionHandle.getCardApplication();
	    Assert.securityConditionDID(cardStateEntry, applicationID, didName, CryptographicServiceActionName.SIGN);

	    byte[] message = sign.getMessage();
	    byte[] keyReference = cryptoMarker.getCryptoKeyInfo().getKeyRef().getKeyRef();
	    byte[] algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getCardAlgRef();
	    byte[] hashRef = cryptoMarker.getAlgorithmInfo().getHashAlgRef();
	    HashGenerationInfoType hashInfo = cryptoMarker.getHashGenerationInfo();

	    if (didStructure.getDIDScope().equals(DIDScopeType.LOCAL)) {
		keyReference[0] = (byte) (0x80 | keyReference[0]);
	    }

	    if (cryptoMarker.getSignatureGenerationInfo() != null) {
		response = performSignature(cryptoMarker, keyReference, algorithmIdentifier, message, slotHandle,
			hashRef, hashInfo);
	    } else {
		// assuming that legacySignatureInformation exists
		BaseTemplateContext templateContext = new BaseTemplateContext();
		templateContext.put(HASHTOSIGN, message);
		templateContext.put(KEYREFERENCE, keyReference);
		templateContext.put(ALGORITHMIDENTIFIER, algorithmIdentifier);
		templateContext.put(HASHALGORITHMREFERENCE, hashRef);
		response = performLegacySignature(cryptoMarker, slotHandle, templateContext);
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.warn(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * This method performs the signature creation according to BSI TR-03112 part 7.
     *
     * @param cryptoMarker The {@link CryptoMarkerType} containing the SignatureCreationInfo for creating the signature.
     * @param keyReference A byte array containing the reference of the key to use.
     * @param algorithmIdentifier A byte array containing the identifier of the signing algorithm.
     * @param message The message to sign.
     * @param slotHandle The slotHandle identifying the card.
     * @param hashRef The variable contains the reference for the hash algorithm which have to be used.
     * @param hashInfo A HashGenerationInfo object which indicates how the hash computation is to perform.
     * @return A {@link SignResponse} object containing the signature of the <b>message</b>.
     * @throws TLVException Thrown if the TLV creation for the key identifier or algorithm identifier failed.
     * @throws IncorrectParameterException Thrown if the SignatureGenerationInfo does not contain PSO_CDS or INT_AUTH
     * after an MSE_KEY command.
     * @throws APDUException Thrown if one of the command to create the signature failed.
     * @throws org.openecard.common.WSHelper.WSException Thrown if the checkResults method of WSHelper failed.
     */
    private SignResponse performSignature(CryptoMarkerType cryptoMarker, byte[] keyReference, byte[] algorithmIdentifier,
	    byte[] message, byte[] slotHandle, byte[] hashRef, HashGenerationInfoType hashInfo) throws TLVException,
	    IncorrectParameterException, APDUException, WSHelper.WSException {
	SignResponse response = WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultOK());

	TLV tagAlgorithmIdentifier = new TLV();
	tagAlgorithmIdentifier.setTagNumWithClass(CARD_ALG_REF);
	tagAlgorithmIdentifier.setValue(algorithmIdentifier);

	TLV tagKeyReference = new TLV();
	tagKeyReference.setTagNumWithClass(KEY_REFERENCE_PRIVATE_KEY);
	tagKeyReference.setValue(keyReference);

	CardCommandAPDU cmdAPDU = null;
	CardResponseAPDU responseAPDU = null;

	String[] signatureGenerationInfo = cryptoMarker.getSignatureGenerationInfo();
	for (String command : signatureGenerationInfo) {
	    HashSet<String> signGenInfo = new HashSet<>(java.util.Arrays.asList(signatureGenerationInfo));

	    if (command.equals("MSE_KEY")) {
		byte[] mseData = tagKeyReference.toBER();

		if (signGenInfo.contains("PSO_CDS")) {
		    cmdAPDU = new ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.DST, mseData);
		} else if (signGenInfo.contains("INT_AUTH") && ! signGenInfo.contains("PSO_CDS")) {
		    cmdAPDU = new ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.AT, mseData);
		} else {
		    String msg = "The command 'MSE_KEY' followed by 'INT_AUTH' and 'PSO_CDS' is currently not supported.";
		    logger.error(msg);
		    throw new IncorrectParameterException(msg);
		}
	    } else if (command.equals("PSO_CDS")) {
		cmdAPDU = new PSOComputeDigitalSignature(message, BLOCKSIZE);
	    } else if (command.equals("INT_AUTH")) {
		cmdAPDU = new InternalAuthenticate(message, BLOCKSIZE);
	    } else if (command.equals("MSE_RESTORE")) {
		cmdAPDU = new ManageSecurityEnvironment.Restore(ManageSecurityEnvironment.DST);
	    } else if (command.equals("MSE_HASH")) {
		cmdAPDU = new ManageSecurityEnvironment.Set(SET_COMPUTATION, ManageSecurityEnvironment.HT);
		TLV mseDataTLV = new TLV();
		mseDataTLV.setTagNumWithClass((byte) 0x80);
		mseDataTLV.setValue(hashRef);
		cmdAPDU.setData(mseDataTLV.toBER());
	    } else if (command.equals("PSO_HASH")) {
		if (hashInfo.value().equals(HashGenerationInfoType.LAST_ROUND_ON_CARD.value()) ||
			hashInfo.value().equals(HashGenerationInfoType.NOT_ON_CARD.value())) {
		    cmdAPDU = new PSOHash(PSOHash.P2_SET_HASH_OR_PART, message);
		} else {
		    cmdAPDU = new PSOHash(PSOHash.P2_HASH_MESSAGE, message);
		}
	    } else if (command.equals("MSE_DS")) {
		byte[] mseData = tagAlgorithmIdentifier.toBER();
		cmdAPDU = new ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.DST, mseData);
	    } else if (command.equals("MSE_KEY_DS")) {
		byte[] mseData = ByteUtils.concatenate(tagKeyReference.toBER(), tagAlgorithmIdentifier.toBER());
		cmdAPDU = new ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.DST, mseData);
	    } else if (command.equals("MSE_INT_AUTH")) {
		byte[] mseData = tagKeyReference.toBER();
		cmdAPDU = new ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.AT, mseData);
	    } else if (command.equals("MSE_KEY_INT_AUTH")) {
		byte[] mseData = ByteUtils.concatenate(tagKeyReference.toBER(), tagAlgorithmIdentifier.toBER());
		cmdAPDU = new ManageSecurityEnvironment(SET_COMPUTATION, ManageSecurityEnvironment.AT, mseData);
	    } else {
		String msg = "The signature generation command '" + command + "' is unknown.";
		throw new IncorrectParameterException(msg);
	    }

	    responseAPDU = cmdAPDU.transmit(dispatcher, slotHandle, Collections.<byte[]>emptyList());
	}

	byte[] signedMessage = responseAPDU.getData();

	// check if further response data is available
	while (responseAPDU.getTrailer()[0] == (byte) 0x61) {
	    GetResponse getResponseData = new GetResponse();
	    responseAPDU = getResponseData.transmit(dispatcher, slotHandle, Collections.<byte[]>emptyList());
	    signedMessage = Arrays.concatenate(signedMessage, responseAPDU.getData());
	}

	if (! Arrays.areEqual(responseAPDU.getTrailer(), new byte[]{(byte) 0x90, (byte) 0x00})) {
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.Disp.COMM_ERROR, responseAPDU.getStatusMessage()));
	    return response;
	}
	
	response.setSignature(signedMessage);
	return response;
    }

    /**
     * The method performs the SignatureCreation if no standard commands are possible.
     * This method creates a signature with APDUs which are not covered by the methods defined in TR-03112 part 7.
     *
     * @param cryptoMarker A {@link CryptoMarkerType} object containing the information about the creation of a signature
     * in a legacy way.
     * @param slotHandle A slotHandle identifying the current card.
     * @param templateCTX A Map containing the context data for the evaluation of the template variables. This object
     * contains per default the message to sign and the {@link TLVFunction}.
     * @return A {@link SignResponse} object containing the signature of the <b>message</b>.
     * @throws APDUTemplateException Thrown if the evaluation of the {@link CardCommandTemplate} failed.
     * @throws APDUException Thrown if one of the commands to execute failed.
     * @throws org.openecard.common.WSHelper.WSException Thrown if the checkResult method of WSHelper failed.
     */
    private SignResponse performLegacySignature(CryptoMarkerType cryptoMarker, byte[] slotHandle,
	    BaseTemplateContext templateCTX) throws APDUTemplateException, APDUException, WSHelper.WSException {
	SignResponse response = WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultOK());
	List<CardCallTemplateType> legacyCommands = cryptoMarker.getLegacySignatureGenerationInfo();
	CardCommandAPDU cmdAPDU;
	CardResponseAPDU responseAPDU = null;
	byte[] signedMessage;

	for (CardCallTemplateType cctt : legacyCommands) {
	    CardCommandTemplate template = new CardCommandTemplate(cctt);
	    cmdAPDU = template.evaluate(templateCTX);
	    responseAPDU = cmdAPDU.transmit(dispatcher, slotHandle, Collections.<byte[]>emptyList());
	}

	signedMessage = responseAPDU.getData();

	// check if further response data is available
	while (responseAPDU.getTrailer()[0] == (byte) 0x61) {
	    CardCommandAPDU getResponseData = new CardCommandAPDU((byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00,
		    responseAPDU.getTrailer()[1]);
	    responseAPDU = getResponseData.transmit(dispatcher, slotHandle, Collections.<byte[]>emptyList());
	    signedMessage = Arrays.concatenate(signedMessage, responseAPDU.getData());
	}

	if (!Arrays.areEqual(responseAPDU.getTrailer(), new byte[]{(byte) 0x90, (byte) 0x00})) {
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.Disp.COMM_ERROR, responseAPDU.getStatusMessage()));
	    return response;
	}

	response.setSignature(signedMessage);
	return response;
    }

}
