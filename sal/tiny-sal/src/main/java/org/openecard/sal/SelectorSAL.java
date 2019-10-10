/****************************************************************************
 * Copyright (C) 2015-2019 ecsec GmbH.
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

package org.openecard.sal;

import iso.std.iso_iec._24727.tech.schema.ACLList;
import iso.std.iso_iec._24727.tech.schema.ACLListResponse;
import iso.std.iso_iec._24727.tech.schema.ACLModify;
import iso.std.iso_iec._24727.tech.schema.ACLModifyResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationCreate;
import iso.std.iso_iec._24727.tech.schema.CardApplicationCreateResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDelete;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationEndSession;
import iso.std.iso_iec._24727.tech.schema.CardApplicationEndSessionResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationSelect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationSelectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceCreate;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceCreateResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDelete;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDescribe;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDescribeResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceListResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceLoad;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceLoadResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSession;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSessionResponse;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CreateSession;
import iso.std.iso_iec._24727.tech.schema.CreateSessionResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDCreate;
import iso.std.iso_iec._24727.tech.schema.DIDCreateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDDelete;
import iso.std.iso_iec._24727.tech.schema.DIDDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DIDUpdate;
import iso.std.iso_iec._24727.tech.schema.DIDUpdateResponse;
import iso.std.iso_iec._24727.tech.schema.DSICreate;
import iso.std.iso_iec._24727.tech.schema.DSICreateResponse;
import iso.std.iso_iec._24727.tech.schema.DSIDelete;
import iso.std.iso_iec._24727.tech.schema.DSIDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DSIList;
import iso.std.iso_iec._24727.tech.schema.DSIListResponse;
import iso.std.iso_iec._24727.tech.schema.DSIRead;
import iso.std.iso_iec._24727.tech.schema.DSIReadResponse;
import iso.std.iso_iec._24727.tech.schema.DSIWrite;
import iso.std.iso_iec._24727.tech.schema.DSIWriteResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetCreate;
import iso.std.iso_iec._24727.tech.schema.DataSetCreateResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetDelete;
import iso.std.iso_iec._24727.tech.schema.DataSetDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetList;
import iso.std.iso_iec._24727.tech.schema.DataSetListResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetSelect;
import iso.std.iso_iec._24727.tech.schema.DataSetSelectResponse;
import iso.std.iso_iec._24727.tech.schema.Decipher;
import iso.std.iso_iec._24727.tech.schema.DecipherResponse;
import iso.std.iso_iec._24727.tech.schema.DestroySession;
import iso.std.iso_iec._24727.tech.schema.DestroySessionResponse;
import iso.std.iso_iec._24727.tech.schema.Encipher;
import iso.std.iso_iec._24727.tech.schema.EncipherResponse;
import iso.std.iso_iec._24727.tech.schema.ExecuteAction;
import iso.std.iso_iec._24727.tech.schema.ExecuteActionResponse;
import iso.std.iso_iec._24727.tech.schema.GetRandom;
import iso.std.iso_iec._24727.tech.schema.GetRandomResponse;
import iso.std.iso_iec._24727.tech.schema.Hash;
import iso.std.iso_iec._24727.tech.schema.HashResponse;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import iso.std.iso_iec._24727.tech.schema.InitializeResponse;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import iso.std.iso_iec._24727.tech.schema.TerminateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.interfaces.Publish;
import org.openecard.common.interfaces.CIFProvider;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.SalSelector;
import org.openecard.common.sal.SpecializedSAL;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.common.util.ByteUtils;
import org.openecard.ws.SAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SAL implementation combining a main SAL and specialized SAL instances.
 * This SAL determines which SAL is best suited to answer a call and then uses this to call the respective function.
 *
 * @author Tobias Wich
 */
public class SelectorSAL implements SAL, CIFProvider, SalSelector {

    private static final Logger LOG = LoggerFactory.getLogger(SelectorSAL.class);

    private final CardRecognition recognition;
    private final SAL main;
    private final LinkedList<SpecializedSAL> special;
    private final LinkedList<SpecializedSAL> initializedSpecializedSals;
    private final LinkedList<SAL> initializedSals;

    public SelectorSAL(SAL mainSal, Environment env) {
	this.recognition = env.getRecognition();
	this.main = mainSal;
	this.special = new LinkedList<>();
        this.initializedSpecializedSals = new LinkedList<>();
	this.initializedSals = new LinkedList<>();
    }

    public void addSpecializedSAL(SpecializedSAL specialSal) {
	this.special.add(specialSal);
    }

    @Override
    public SAL getSalForHandle(ConnectionHandleType handle) {
	for (SpecializedSAL sal : initializedSpecializedSals) {
	    if (sal.specializedFor(handle)) {
		return sal;
	    }
	}
	return main;
    }

    @Override
    public SAL getSalForPath(CardApplicationPathType path) {
	for (SpecializedSAL sal : initializedSpecializedSals) {
	    if (sal.specializedFor(path)) {
		return sal;
	    }
	}
	return main;
    }

    @Override
    public List<SAL> getSalForProtocol(String protocolUri) {
	ArrayList<SAL> result = new ArrayList<>();
	for (SAL next : initializedSals) {
	    if (next.supportedProtocols().contains(protocolUri)) {
		result.add(next);
	    }
	}
	return result;
    }

    @Override
    public SAL getSalForCardType(String cardType) {
        for (SpecializedSAL sal : initializedSpecializedSals) {
            if (sal.specializedFor(cardType)) {
                return sal;
            }
        }
        return main;
    }

    @Override
    public Set<String> supportedProtocols() {
	TreeSet result = new TreeSet<>();
	for (SAL next : initializedSals) {
	    result.addAll(next.supportedProtocols());
	}
	return result;
    }


    @Override
    public CardInfoType getCardInfo(@Nonnull ConnectionHandleType type, String cardType) {
	LOG.debug("Looking up responsible SAL for handle with, ctx={}, slot={}",
		ByteUtils.toHexString(type.getContextHandle()), ByteUtils.toHexString(type.getSlotHandle()));
	SAL sal = getSalForHandle(type);
	// only ask special SAL when we have a handle and a special SAL which is a CIF provider
	if (sal instanceof CIFProvider) {
	    LOG.debug("Requesting CIF from Specialized SAL for type={}.", cardType);
	    return ((CIFProvider) sal).getCardInfo(type, cardType);
	} else {
	    LOG.debug("Requesting CIF from CIF-Repo for type={}.", cardType);
	    return recognition.getCardInfoFromRepo(cardType);
	}
    }

    @Override
    public CardInfoType getCardInfo(String cardType) throws RuntimeException {
        SAL sal = getSalForCardType(cardType);
        if (sal instanceof CIFProvider) {
	    LOG.debug("Requesting CIF from Specialized SAL for type={}.", cardType);
            return ((CIFProvider) sal).getCardInfo(cardType);
        } else {
	    LOG.debug("Requesting CIF from CIF-Repo for type={}.", cardType);
            return recognition.getCardInfoFromRepo(cardType);
        }
    }

    @Override
    public InputStream getCardImage(String cardType) {
        SAL sal = getSalForCardType(cardType);
        if (sal instanceof CIFProvider) {
            return ((CIFProvider) sal).getCardImage(cardType);
        } else {
            return null;
        }
    }

    @Override
    public boolean needsRecognition(byte[] atr) {
	for (SpecializedSAL next : initializedSpecializedSals) {
	    if (next instanceof CIFProvider) {
		CIFProvider cp = (CIFProvider) next;
		if (! cp.needsRecognition(atr)) {
		    return false;
		}
	    }
	}

	if (main instanceof CIFProvider) {
	    CIFProvider cp = (CIFProvider) main;
	    return cp.needsRecognition(atr);
	} else {
	    return true;
	}
    }

    @Override
    public InitializeResponse initialize(Initialize init) {
	InitializeResponse response = WSHelper.makeResponse(InitializeResponse.class, WSHelper.makeResultOK());

	// only initialize one time
	if (initializedSals.isEmpty()) {
	    LinkedList<SAL> sals = new LinkedList<>();
	    sals.add(main);
	    sals.addAll(special);

	    for (SAL next : sals) {
		try {
		    // first add it to list of initialized sals, remove later if error
		    if (next instanceof SpecializedSAL) {
			initializedSpecializedSals.add((SpecializedSAL) next);
		    } else {
			initializedSals.add(next);
		    }

		    InitializeResponse res = next.initialize(init);
		    WSHelper.checkResult(res);
		    if (WSHelper.resultsInWarn(res)) {
			WSHelper.createException(res.getResult());
		    }
		} catch (WSHelper.WSException ex) {
		    String msg = "One of the SAL instances failed to initialize:\n" + ex.getMessage();
		    terminate(new Terminate());
		    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.Disp.COMM_ERROR, msg));
		    return response;
		}
	    }
	}

	return response;
    }

    @Override
    public TerminateResponse terminate(Terminate terminate) {
	TerminateResponse response = WSHelper.makeResponse(TerminateResponse.class, WSHelper.makeResultOK());

	// only terminate if initialized
	if (! initializedSals.isEmpty()) {
	    boolean errorHit = false;
	    for (SAL next : initializedSals) {
		try {
		    TerminateResponse res = next.terminate(terminate);
		    WSHelper.checkResult(res);
		} catch (WSHelper.WSException ex) {
		    // in case of an error, take the error code from the first SAL
		    if (! errorHit) {
			response.setResult(ex.getResult());
		    }
		}
	    }

	    initializedSals.clear();
	}

	return response;
    }

    @Override
    public CreateSessionResponse createSession(CreateSession parameters) {
	return WSHelper.makeResponse(CreateSessionResponse.class, WSHelper.makeResultError(
		ECardConstants.Minor.SAL.PREREQUISITES_NOT_SATISFIED,
		"Selector SAL does not implement the CreateSession method."));
    }

    @Override
    public DestroySessionResponse destroySession(DestroySession parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.destroySession(parameters);
    }

    @Override
    public CardApplicationPathResponse cardApplicationPath(CardApplicationPath parameters) {
	return getSalForPath(parameters.getCardAppPathRequest())
		.cardApplicationPath(parameters);
    }

    @Override
    public CardApplicationConnectResponse cardApplicationConnect(CardApplicationConnect parameters) {
	return getSalForPath(parameters.getCardApplicationPath())
		.cardApplicationConnect(parameters);
    }

    @Override
    public CardApplicationSelectResponse cardApplicationSelect(CardApplicationSelect parameters) {
	return getSalForHandle(SALUtils.createConnectionHandle(parameters.getSlotHandle()))
		.cardApplicationSelect(parameters);
    }

    @Override
    public CardApplicationDisconnectResponse cardApplicationDisconnect(CardApplicationDisconnect parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationDisconnect(parameters);
    }

    @Publish
    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationStartSession(parameters);
    }

    @Publish
    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationEndSession(parameters);
    }

    @Publish
    @Override
    public CardApplicationListResponse cardApplicationList(CardApplicationList parameters) {
	return main.cardApplicationList(parameters);
    }

    @Override
    public CardApplicationCreateResponse cardApplicationCreate(CardApplicationCreate parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationCreate(parameters);
    }

    @Override
    public CardApplicationDeleteResponse cardApplicationDelete(CardApplicationDelete parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationDelete(parameters);
    }

    @Publish
    @Override
    public CardApplicationServiceListResponse cardApplicationServiceList(CardApplicationServiceList parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationServiceList(parameters);
    }

    @Override
    public CardApplicationServiceCreateResponse cardApplicationServiceCreate(CardApplicationServiceCreate parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationServiceCreate(parameters);
    }

    @Override
    public CardApplicationServiceLoadResponse cardApplicationServiceLoad(CardApplicationServiceLoad parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationServiceLoad(parameters);
    }

    @Override
    public CardApplicationServiceDeleteResponse cardApplicationServiceDelete(CardApplicationServiceDelete parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationServiceDelete(parameters);
    }

    @Publish
    @Override
    public CardApplicationServiceDescribeResponse cardApplicationServiceDescribe(CardApplicationServiceDescribe parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.cardApplicationServiceDescribe(parameters);
    }

    @Override
    public ExecuteActionResponse executeAction(ExecuteAction parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.executeAction(parameters);
    }

    @Publish
    @Override
    public DataSetListResponse dataSetList(DataSetList parameters) {
	return main.dataSetList(parameters);
    }

    @Override
    public DataSetCreateResponse dataSetCreate(DataSetCreate parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.dataSetCreate(parameters);
    }

    @Publish
    @Override
    public DataSetSelectResponse dataSetSelect(DataSetSelect parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.dataSetSelect(parameters);
    }

    @Override
    public DataSetDeleteResponse dataSetDelete(DataSetDelete parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.dataSetDelete(parameters);
    }

    @Publish
    @Override
    public DSIListResponse dsiList(DSIList parameters) {
	return main.dsiList(parameters);
    }

    @Override
    public DSICreateResponse dsiCreate(DSICreate parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.dsiCreate(parameters);
    }

    //TODO: rewiew function and add @Publish annotation
    @Override
    public DSIDeleteResponse dsiDelete(DSIDelete parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.dsiDelete(parameters);
    }

    @Publish
    @Override
    public DSIWriteResponse dsiWrite(DSIWrite parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.dsiWrite(parameters);
    }

    @Publish
    @Override
    public DSIReadResponse dsiRead(DSIRead parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.dsiRead(parameters);
    }

    @Publish
    @Override
    public EncipherResponse encipher(Encipher parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.encipher(parameters);
    }

    @Publish
    @Override
    public DecipherResponse decipher(Decipher parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.decipher(parameters);
    }

    @Publish
    @Override
    public GetRandomResponse getRandom(GetRandom parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.getRandom(parameters);
    }

    @Publish
    @Override
    public HashResponse hash(Hash parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.hash(parameters);
    }

    @Publish
    @Override
    public SignResponse sign(Sign parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.sign(parameters);
    }

    @Publish
    @Override
    public VerifySignatureResponse verifySignature(VerifySignature parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.verifySignature(parameters);
    }

    @Publish
    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.verifyCertificate(parameters);
    }

    @Publish
    @Override
    public DIDListResponse didList(DIDList parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.didList(parameters);
    }

    @Override
    public DIDCreateResponse didCreate(DIDCreate parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.didCreate(parameters);
    }

    @Publish
    @Override
    public DIDGetResponse didGet(DIDGet parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.didGet(parameters);
    }

    @Override
    public DIDUpdateResponse didUpdate(DIDUpdate parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.didUpdate(parameters);
    }

    @Override
    public DIDDeleteResponse didDelete(DIDDelete parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.didDelete(parameters);
    }

    @Publish
    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.didAuthenticate(parameters);
    }

    @Publish
    @Override
    public ACLListResponse aclList(ACLList parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.aclList(parameters);
    }

    @Override
    public ACLModifyResponse aclModify(ACLModify parameters) {
	return getSalForHandle(parameters.getConnectionHandle())
		.aclModify(parameters);
    }

}
