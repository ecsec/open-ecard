/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.*;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.common.ECardConstants;
import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.state.StateEntry;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.crypto.common.HashAlgorithms;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.crypto.common.sal.did.CryptoMarkerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


/**
 * Implements the Hash step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.8.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class HashStep implements ProtocolStep<Hash, HashResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(HashStep.class);
    private final Dispatcher dispatcher;

    /**
     * Creates a new HashStep.
     *
     * @param dispatcher Dispatcher
     */
    public HashStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.Hash;
    }

    @Override
    public HashResponse perform(Hash request, Map<String, Object> internalData) {
	HashResponse response = WSHelper.makeResponse(HashResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    String didName = SALUtils.getDIDName(request);
	    StateEntry cardStateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle);
	    DIDStructureType didStructure = SALUtils.getDIDStructure(request, didName, cardStateEntry, connectionHandle);
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didStructure.getDIDMarker());

	    HashGenerationInfoType hashInfo = cryptoMarker.getHashGenerationInfo();
	    if (hashInfo != null) {
		if (hashInfo == HashGenerationInfoType.NOT_ON_CARD) {
		    String algId = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();
		    SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algId);
		    HashAlgorithms hashAlg = alg.getHashAlg();
		    if (hashAlg == null) {
			String msg = String.format("Algorithm %s does not specify a Hash algorithm.", algId);
			LOG.error(msg);
			String minor = ECardConstants.Minor.App.INCORRECT_PARM;
			response.setResult(WSHelper.makeResultError(minor, msg));
		    } else {
			// calculate hash
			MessageDigest md = MessageDigest.getInstance(hashAlg.getJcaAlg());
			md.update(request.getMessage());
			byte[] digest = md.digest();
			response.setHash(digest);
		    }
		} else {
		    // TODO: implement hashing on card
		    String msg = String.format("Unsupported Hash generation type (%s) requested.", hashInfo);
		    LOG.error(msg);
		    String minor = ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION;
		    response.setResult(WSHelper.makeResultError(minor, msg));
		}
	    } else {
		// no hash alg specified, this is an error
		String msg = String.format("No Hash generation type specified in CIF.");
		LOG.error(msg);
		String minor = ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION;
		response.setResult(WSHelper.makeResultError(minor, msg));
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (UnsupportedAlgorithmException | NoSuchAlgorithmException ex) {

	} catch (Exception e) {
	    LOG.warn(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

}
