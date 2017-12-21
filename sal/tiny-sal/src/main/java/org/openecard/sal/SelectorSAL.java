/****************************************************************************
 * Copyright (C) 2015-2017 ecsec GmbH.
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
import java.util.LinkedList;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.interfaces.Publish;
import org.openecard.common.interfaces.CIFProvider;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.sal.SpecializedSAL;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.ws.SAL;


/**
 * SAL implementation combining a main SAL and specialized SAL instances.
 * This SAL determines which SAL is best suited to answer a call and then uses this to call the respective function.
 *
 * @author Tobias Wich
 */
public class SelectorSAL implements SAL, CIFProvider {

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

    private SAL getResponsibleSAL(CardApplicationPathType path) {
	for (SpecializedSAL sal : initializedSpecializedSals) {
	    if (sal.specializedFor(path)) {
		return sal;
	    }
	}
	return main;
    }

    private SAL getResponsibleSAL(ConnectionHandleType handle) {
	for (SpecializedSAL sal : initializedSpecializedSals) {
	    if (sal.specializedFor(handle)) {
		return sal;
	    }
	}
	return main;
    }

    private SAL getResponsibleSAL(String cardType) {
        for (SpecializedSAL sal : initializedSpecializedSals) {
            if (sal.specializedFor(cardType)) {
                return sal;
            }
        }
        return main;
    }


    @Override
    public CardInfoType getCardInfo(ConnectionHandleType type, String cardType) {
	SAL sal = getResponsibleSAL(type);
	// only ask special SAL when we have a handle and a special SAL which is a CIF provider
	if (type != null && sal instanceof CIFProvider) {
	    return ((CIFProvider) sal).getCardInfo(type, cardType);
	} else {
	    return recognition.getCardInfoFromRepo(cardType);
	}
    }

    @Override
    public CardInfoType getCardInfo(String cardType) throws RuntimeException {
        SAL sal = getResponsibleSAL(cardType);
        if (sal instanceof CIFProvider) {
            return ((CIFProvider) sal).getCardInfo(cardType);
        } else {
            return null;
        }
    }

    @Override
    public InputStream getCardImage(String cardType) {
        SAL sal = getResponsibleSAL(cardType);
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
		    InitializeResponse res = next.initialize(init);
		    WSHelper.checkResult(res);
		    if (! WSHelper.resultsInWarn(res)) {
			if (next instanceof SpecializedSAL) {
			    initializedSpecializedSals.add((SpecializedSAL) next);
			} else {
			    initializedSals.add(next);
			}
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
    public CardApplicationPathResponse cardApplicationPath(CardApplicationPath parameters) {
	return main.cardApplicationPath(parameters);
    }

    @Override
    public CardApplicationConnectResponse cardApplicationConnect(CardApplicationConnect parameters) {
	return getResponsibleSAL(parameters.getCardApplicationPath())
		.cardApplicationConnect(parameters);
    }

    @Override
    public CardApplicationSelectResponse cardApplicationSelect(CardApplicationSelect parameters) {
	return getResponsibleSAL(SALUtils.createConnectionHandle(parameters.getSlotHandle()))
		.cardApplicationSelect(parameters);
    }

    @Override
    public CardApplicationDisconnectResponse cardApplicationDisconnect(CardApplicationDisconnect parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationDisconnect(parameters);
    }

    @Publish
    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationStartSession(parameters);
    }

    @Publish
    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationEndSession(parameters);
    }

    @Publish
    @Override
    public CardApplicationListResponse cardApplicationList(CardApplicationList parameters) {
	return main.cardApplicationList(parameters);
    }

    @Override
    public CardApplicationCreateResponse cardApplicationCreate(CardApplicationCreate parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationCreate(parameters);
    }

    @Override
    public CardApplicationDeleteResponse cardApplicationDelete(CardApplicationDelete parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationDelete(parameters);
    }

    @Publish
    @Override
    public CardApplicationServiceListResponse cardApplicationServiceList(CardApplicationServiceList parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationServiceList(parameters);
    }

    @Override
    public CardApplicationServiceCreateResponse cardApplicationServiceCreate(CardApplicationServiceCreate parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationServiceCreate(parameters);
    }

    @Override
    public CardApplicationServiceLoadResponse cardApplicationServiceLoad(CardApplicationServiceLoad parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationServiceLoad(parameters);
    }

    @Override
    public CardApplicationServiceDeleteResponse cardApplicationServiceDelete(CardApplicationServiceDelete parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationServiceDelete(parameters);
    }

    @Publish
    @Override
    public CardApplicationServiceDescribeResponse cardApplicationServiceDescribe(CardApplicationServiceDescribe parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.cardApplicationServiceDescribe(parameters);
    }

    @Override
    public ExecuteActionResponse executeAction(ExecuteAction parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.executeAction(parameters);
    }

    @Publish
    @Override
    public DataSetListResponse dataSetList(DataSetList parameters) {
	return main.dataSetList(parameters);
    }

    @Override
    public DataSetCreateResponse dataSetCreate(DataSetCreate parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.dataSetCreate(parameters);
    }

    @Publish
    @Override
    public DataSetSelectResponse dataSetSelect(DataSetSelect parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.dataSetSelect(parameters);
    }

    @Override
    public DataSetDeleteResponse dataSetDelete(DataSetDelete parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.dataSetDelete(parameters);
    }

    @Publish
    @Override
    public DSIListResponse dsiList(DSIList parameters) {
	return main.dsiList(parameters);
    }

    @Override
    public DSICreateResponse dsiCreate(DSICreate parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.dsiCreate(parameters);
    }

    //TODO: rewiew function and add @Publish annotation
    @Override
    public DSIDeleteResponse dsiDelete(DSIDelete parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.dsiDelete(parameters);
    }

    @Publish
    @Override
    public DSIWriteResponse dsiWrite(DSIWrite parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.dsiWrite(parameters);
    }

    @Publish
    @Override
    public DSIReadResponse dsiRead(DSIRead parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.dsiRead(parameters);
    }

    @Publish
    @Override
    public EncipherResponse encipher(Encipher parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.encipher(parameters);
    }

    @Publish
    @Override
    public DecipherResponse decipher(Decipher parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.decipher(parameters);
    }

    @Publish
    @Override
    public GetRandomResponse getRandom(GetRandom parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.getRandom(parameters);
    }

    @Publish
    @Override
    public HashResponse hash(Hash parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.hash(parameters);
    }

    @Publish
    @Override
    public SignResponse sign(Sign parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.sign(parameters);
    }

    @Publish
    @Override
    public VerifySignatureResponse verifySignature(VerifySignature parameters) {
	// TODO: check if main sal is better doing this
	return getResponsibleSAL(parameters.getConnectionHandle())
		.verifySignature(parameters);
    }

    @Publish
    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate parameters) {
	// TODO: check if main sal is better doing this
	return getResponsibleSAL(parameters.getConnectionHandle())
		.verifyCertificate(parameters);
    }

    @Publish
    @Override
    public DIDListResponse didList(DIDList parameters) {
	return main.didList(parameters);
    }

    @Override
    public DIDCreateResponse didCreate(DIDCreate parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.didCreate(parameters);
    }

    @Publish
    @Override
    public DIDGetResponse didGet(DIDGet parameters) {
	return main.didGet(parameters);
    }

    @Override
    public DIDUpdateResponse didUpdate(DIDUpdate parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.didUpdate(parameters);
    }

    @Override
    public DIDDeleteResponse didDelete(DIDDelete parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.didDelete(parameters);
    }

    @Publish
    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.didAuthenticate(parameters);
    }

    @Publish
    @Override
    public ACLListResponse aclList(ACLList parameters) {
	return main.aclList(parameters);
    }

    @Override
    public ACLModifyResponse aclModify(ACLModify parameters) {
	return getResponsibleSAL(parameters.getConnectionHandle())
		.aclModify(parameters);
    }

}
