/****************************************************************************
 * Copyright (C) 2015-2018 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import org.openecard.mdlw.sal.config.MiddlewareSALConfig;
import iso.std.iso_iec._24727.tech.schema.ACLList;
import iso.std.iso_iec._24727.tech.schema.ACLListResponse;
import iso.std.iso_iec._24727.tech.schema.ACLModify;
import iso.std.iso_iec._24727.tech.schema.ACLModifyResponse;
import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType;
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
import iso.std.iso_iec._24727.tech.schema.ConnectionServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DIDCreate;
import iso.std.iso_iec._24727.tech.schema.DIDCreateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDDelete;
import iso.std.iso_iec._24727.tech.schema.DIDDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DIDUpdate;
import iso.std.iso_iec._24727.tech.schema.DIDUpdateDataType;
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
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
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
import iso.std.iso_iec._24727.tech.schema.NamedDataServiceActionName;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import iso.std.iso_iec._24727.tech.schema.TerminateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.ECardException;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.WSHelper;
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateInputType;
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateOutputType;
import org.openecard.common.anytype.pin.PINCompareMarkerType;
import org.openecard.common.interfaces.CIFProvider;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.SpecializedSAL;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.exception.InternalAppError;
import org.openecard.common.sal.exception.NamedEntityNotFoundException;
import org.openecard.common.sal.exception.UnknownProtocolException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.common.util.ByteComparator;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;
import org.openecard.common.util.ValueGenerators;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.crypto.common.sal.did.CryptoMarkerType;
import org.openecard.gui.UserConsent;
import org.openecard.mdlw.event.MwEventManager;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.openecard.mdlw.sal.enums.UserType;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.FinalizationException;
import org.openecard.mdlw.sal.exceptions.InitializationException;
import org.openecard.mdlw.sal.exceptions.PinBlockedException;
import org.openecard.mdlw.sal.exceptions.PinIncorrectException;
import org.openecard.mdlw.sal.exceptions.TokenException;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a Service Access Layer (SAL) for PKCS11 middleware drivers.
 *
 * @author Tobias Wich
 */
public class MiddlewareSAL implements SpecializedSAL, CIFProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MiddlewareSAL.class);

    private final Environment env;
    private final CardStateMap states;
    private final byte[] ctxHandle;
    private final MwEventManager eventMan;
    private final boolean builtinPinDialog;
    private UserConsent gui;

    private final TreeMap<byte[], MwSlot> managedSlots;
    private final TreeMap<byte[], MwSession> managedSessions;

    private final MwModule mwModule;
    private final MiddlewareSALConfig mwSALConfig;

    /**
     * Creates a new TinySAL.
     *
     * @param env Environment
     * @param states CardStateMap
     * @param mwSALConfig MiddlewareSALConfig
     */
    public MiddlewareSAL(Environment env, CardStateMap states, MiddlewareSALConfig mwSALConfig) {
        this.env = env;
        this.states = states;
        this.mwSALConfig = mwSALConfig;
        this.ctxHandle = ValueGenerators.generateRandom(32);
        this.eventMan = new MwEventManager(env, this, ctxHandle);
	this.builtinPinDialog = mwSALConfig.hasBuiltinPinDialog();

        managedSlots = new TreeMap<>(new ByteComparator());
        managedSessions = new TreeMap<>(new ByteComparator());

        mwModule = new MwModule(mwSALConfig);
    }


    public void setGui(UserConsent gui) {
	this.gui = gui;
    }

    public MwModule getMwModule() {
        return this.mwModule;
    }

    public MiddlewareSALConfig getMiddlewareSALConfig() {
        return this.mwModule.getMiddlewareSALConfig();
    }

    @Override
    public String getMiddlewareSALName() {
        return mwSALConfig.getMiddlewareName();
    }

    @Override
    public boolean specializedFor(CardApplicationPathType path) {
        return ByteUtils.compare(ctxHandle, path.getContextHandle());
    }

    @Override
    public boolean specializedFor(ConnectionHandleType handle) {
        return managedSlots.containsKey(handle.getSlotHandle())
                || ByteUtils.compare(ctxHandle, handle.getContextHandle());
    }

    @Override
    public boolean specializedFor(String cardType) {
        return mwSALConfig.isCardTypeKnown(cardType);
    }

    @Override
    public boolean needsRecognition(byte[] atr) {
	return ! mwSALConfig.isATRKnown(atr);
    }


    @Override
    public CardInfoType getCardInfo(String cardType) throws RuntimeException {
        return mwSALConfig.getCardInfo(cardType);
    }

    @Override
    public InputStream getCardImage(String cardType) {
        return mwSALConfig.getCardImage(cardType);
    }


    @Override
    public CardInfoType getCardInfo(@Nonnull ConnectionHandleType handle, @Nonnull String cardType)
	    throws RuntimeException {
        CardInfoType cif = mwSALConfig.getCardInfo(cardType);
	if (cif != null) {
	    cif = augmentCardInfo(handle, cif);
	    return cif;
	} else {
	    LOG.error("No CIF available for card type '" + cardType + '"');
	    return null;
	}
    }

    private CardInfoType augmentCardInfo(@Nonnull ConnectionHandleType handle, @Nonnull CardInfoType template) {
	boolean needsConnect = handle.getSlotHandle() == null;
	try {
	    // connect card, so that we have a session
	    MwSession session;
	    if (needsConnect) {
		MwSlot slot = getMatchingSlot(handle.getIFDName(), handle.getSlotIndex());
		if (slot != null) {
		    session = slot.openSession();
		} else {
		     throw new TokenException("No card available in this slot.", CryptokiLibrary.CKR_TOKEN_NOT_PRESENT);
		}
	    } else {
		session = managedSessions.get(handle.getSlotHandle());
	    }

	    if (session != null) {
		CIFCreator cc = new CIFCreator(session, template);
		CardInfoType cif = cc.addTokenInfo();
		LOG.info("Finished augmenting CardInfo file.");
		return cif;
	    } else {
		LOG.warn("Card not available for object info retrieval anymore.");
		return null;
	    }
	} catch (WSMarshallerException ex) {
	    throw new RuntimeException("Failed to marshal CIF file.", ex);
	} catch (CryptokiException ex) {
	    throw new RuntimeException("Error in PKCS#11 module while requesting CIF data.", ex);
	}
    }

    @Override
    public InitializeResponse initialize(Initialize parameters) {
        InitializeResponse resp = WSHelper.makeResponse(InitializeResponse.class, WSHelper.makeResultOK());
        try {
            mwModule.initialize();
            eventMan.initialize();

	    if (gui == null) {
		throw new InternalAppError("GUI is not initialized.");
	    }
	} catch (UnsatisfiedLinkError | InitializationException ex) {
            String mwSALName = mwSALConfig.getMiddlewareName();
            String msg = String.format("Failed to initialize Middleware for '%s'-SAL.", mwSALName);
	    if (mwSALConfig.isSALRequired()) {
		LOG.error(msg, ex);
		resp.setResult(WSHelper.makeResultError(ECardConstants.Minor.Disp.COMM_ERROR, msg));
	    } else {
		LOG.warn(msg, ex);
		resp.setResult(WSHelper.makeResult(ECardConstants.Major.WARN,
			ECardConstants.Minor.App.NOT_INITIALIZED, msg));
	    }
        } catch (InternalAppError ex) {
	    LOG.error(ex.getMessage());
	    resp.setResult(ex.getResult());
	}

        return resp;
    }

    @Override
    public TerminateResponse terminate(Terminate parameters) {
        TerminateResponse resp = WSHelper.makeResponse(TerminateResponse.class, WSHelper.makeResultOK());
        try {
            eventMan.terminate();
            mwModule.destroy();
        } catch (FinalizationException ex) {
            String msg = "Failed to terminate Middleware.";
            LOG.error(msg, ex);
            resp.setResult(WSHelper.makeResultError(ECardConstants.Minor.Disp.COMM_ERROR, msg));
        }
        return resp;
    }

    @Override
    public CardApplicationPathResponse cardApplicationPath(CardApplicationPath parameters) {
        throw new UnsupportedOperationException("Not supported, should be handled by main SAL.");
    }

    @Nullable
    private MwSlot getMatchingSlot(String ifdName, BigInteger idx) throws CryptokiException {
	for (MwSlot slot : mwModule.getSlotList(true)) {
	    // IFD name must match
	    String slotIfdName = slot.getSlotInfo().getSlotDescription();
	    long slotId = slot.getSlotInfo().getSlotID();
	    if (slotIfdName.equals(ifdName) && idx != null && slotId == idx.longValue()) {
		return slot;
	    }
	}
	return null;
    }

    @Override
    public CardApplicationConnectResponse cardApplicationConnect(CardApplicationConnect request) {
        CardApplicationConnectResponse response = WSHelper.makeResponse(CardApplicationConnectResponse.class,
                WSHelper.makeResultOK());

        try {
            CardApplicationPathType cardAppPath = request.getCardApplicationPath();
            Assert.assertIncorrectParameter(cardAppPath, "The parameter CardAppPathRequest is empty.");

            Set<CardStateEntry> cardStateEntrySet = states.getMatchingEntries(cardAppPath, false);
            Assert.assertIncorrectParameter(cardStateEntrySet, "The given ConnectionHandle is invalid.");

            /*
	     * [TR-03112-4] If the provided path fragments are valid for more than one card application
	     * the eCard-API-Framework SHALL return any of the possible choices.
             */
            CardStateEntry cardStateEntry = cardStateEntrySet.iterator().next();
            ConnectionHandleType handle = cardStateEntry.handleCopy();
	    cardStateEntry = cardStateEntry.derive(handle);
            byte[] applicationID = cardStateEntry.getImplicitlySelectedApplicationIdentifier();
            Assert.securityConditionApplication(cardStateEntry, applicationID,
                    ConnectionServiceActionName.CARD_APPLICATION_CONNECT);

            // find matching slot and associate it with the slotHandle
	    MwSlot slot = getMatchingSlot(handle.getIFDName(), handle.getSlotIndex());
            if (slot != null) {
		// open session
		MwSession session = slot.openSession();
		// save values in maps
		byte[] slotHandle = ValueGenerators.generateRandom(64);
		handle.setSlotHandle(slotHandle);
		managedSlots.put(slotHandle, slot);
		managedSessions.put(slotHandle, session);
            } else {
                throw new IncorrectParameterException("No slot found for requestet handle.");
            }

            cardStateEntry.setSlotHandle(handle.getSlotHandle());
            // reset the ef FCP
            cardStateEntry.unsetFCPOfSelectedEF();
            states.addEntry(cardStateEntry);

            response.setConnectionHandle(cardStateEntry.handleCopy());
            response.getConnectionHandle().setCardApplication(applicationID);
        } catch (ECardException e) {
            response.setResult(e.getResult());
        } catch (CryptokiException ex) {
            String msg = "Error in Middleware.";
            LOG.error(msg, ex);
            response.setResult(WSHelper.makeResultError(ECardConstants.Minor.Disp.COMM_ERROR, msg));
        }

        return response;
    }

    @Override
    public CardApplicationDisconnectResponse cardApplicationDisconnect(CardApplicationDisconnect request) {
        CardApplicationDisconnectResponse response = WSHelper.makeResponse(CardApplicationDisconnectResponse.class,
                WSHelper.makeResultOK());

        try {
            ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            byte[] slotHandle = connectionHandle.getSlotHandle();

            // check existence of required parameters
            if (slotHandle == null) {
                return WSHelper.makeResponse(CardApplicationDisconnectResponse.class,
                        WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "ConnectionHandle is null"));
            }

            managedSlots.remove(slotHandle);
            MwSession session = managedSessions.remove(slotHandle);

	    if (session != null) {
		session.closeSession();
	    }

            // remove entries associated with this handle
            states.removeSlotHandleEntry(slotHandle);
        } catch (ECardException e) {
            response.setResult(e.getResult());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
            response.setResult(WSHelper.makeResult(e));
        }

        return response;
    }

    @Override
    public CardApplicationSelectResponse cardApplicationSelect(CardApplicationSelect parameters) {
        CardApplicationSelectResponse response = WSHelper.makeResponse(CardApplicationSelectResponse.class,
                WSHelper.makeResultOK());

        try {
            ConnectionHandleType handle = SALUtils.createConnectionHandle(parameters.getSlotHandle());
            CardStateEntry entry = states.getEntry(handle);
            Assert.assertConnectionHandle(entry, handle);

            // get fully filled handle
            handle = entry.handleCopy();
            response.setConnectionHandle(handle);
            return response;
        } catch (ECardException ex) {
            response.setResult(ex.getResult());
        }

        return response;
    }

    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CardApplicationListResponse cardApplicationList(CardApplicationList parameters) {
        throw new UnsupportedOperationException("Not supported, should be handled by main SAL.");
    }

    @Override
    public CardApplicationCreateResponse cardApplicationCreate(CardApplicationCreate parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CardApplicationDeleteResponse cardApplicationDelete(CardApplicationDelete parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CardApplicationServiceListResponse cardApplicationServiceList(CardApplicationServiceList parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CardApplicationServiceCreateResponse cardApplicationServiceCreate(CardApplicationServiceCreate parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CardApplicationServiceLoadResponse cardApplicationServiceLoad(CardApplicationServiceLoad parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CardApplicationServiceDeleteResponse cardApplicationServiceDelete(CardApplicationServiceDelete parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CardApplicationServiceDescribeResponse cardApplicationServiceDescribe(CardApplicationServiceDescribe parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ExecuteActionResponse executeAction(ExecuteAction parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataSetListResponse dataSetList(DataSetList parameters) {
        throw new UnsupportedOperationException("Not supported, should be handled by main SAL.");
    }

    @Override
    public DataSetCreateResponse dataSetCreate(DataSetCreate parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataSetSelectResponse dataSetSelect(DataSetSelect request) {
        DataSetSelectResponse response = WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResultOK());

        try {
            ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
            byte[] applicationID = connectionHandle.getCardApplication();
            String dataSetName = request.getDataSetName();

            Assert.assertIncorrectParameter(dataSetName, "The parameter DataSetName is empty.");

            CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
            DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSet(dataSetName, applicationID);
            Assert.assertNamedEntityNotFound(dataSetInfo, "The given DataSet cannot be found.");

            Assert.securityConditionDataSet(cardStateEntry, applicationID, dataSetName,
                    NamedDataServiceActionName.DATA_SET_SELECT);

            // nothing else to do, DSI Read works for itself
        } catch (ECardException e) {
            response.setResult(e.getResult());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
            response.setResult(WSHelper.makeResult(e));
        }

        return response;
    }

    @Override
    public DataSetDeleteResponse dataSetDelete(DataSetDelete parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DSIListResponse dsiList(DSIList parameters) {
        throw new UnsupportedOperationException("Not supported, should be handled by main SAL.");
    }

    @Override
    public DSICreateResponse dsiCreate(DSICreate parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DSIDeleteResponse dsiDelete(DSIDelete parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DSIWriteResponse dsiWrite(DSIWrite parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DSIReadResponse dsiRead(DSIRead request) {
        DSIReadResponse response = WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultOK());

        try {
            ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
            byte[] applicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
            String dsiName = request.getDSIName();
            byte[] slotHandle = connectionHandle.getSlotHandle();
            Assert.assertIncorrectParameter(dsiName, "The parameter DSIName is empty.");
            Assert.securityConditionDataSet(cardStateEntry, applicationID, dsiName, NamedDataServiceActionName.DSI_READ);

            MwSession session = managedSessions.get(slotHandle);

            for (MwCertificate cert : session.getCertificates()) {
                String label = cert.getLabel();
                if (label.equals(dsiName)) {
                    // read certificate
                    byte[] certificate = cert.getValue();

                    response.setDSIContent(certificate);
                    return response;
                }
            }

            String msg = "The given DSIName does not related to any know DSI or DataSet.";
            throw new IncorrectParameterException(msg);

        } catch (ECardException e) {
            response.setResult(e.getResult());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
            response.setResult(WSHelper.makeResult(e));
        }

        return response;
    }

    @Override
    public EncipherResponse encipher(Encipher parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DecipherResponse decipher(Decipher parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GetRandomResponse getRandom(GetRandom parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HashResponse hash(Hash parameters) {
        HashResponse response = WSHelper.makeResponse(HashResponse.class, WSHelper.makeResultOK());

	// bouncy the message because I assume the hash is calculated by the sign function
	response.setHash(parameters.getMessage());
	return response;
    }

    @Override
    public SignResponse sign(Sign request) {
        SignResponse response = WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultOK());

        try {
            ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
            byte[] application = cardStateEntry.getImplicitlySelectedApplicationIdentifier();
            byte[] slotHandle = connectionHandle.getSlotHandle();
            String didName = SALUtils.getDIDName(request);
            byte[] message = request.getMessage();
            Assert.assertIncorrectParameter(message, "The parameter Message is empty.");

            DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, application);
            Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

            CryptoMarkerType marker = new CryptoMarkerType(didStructure.getDIDMarker());
            String keyLabel = marker.getLegacyKeyName();

            MwSession session = managedSessions.get(slotHandle);
            for (MwPrivateKey key : session.getPrivateKeys()) {
		LOG.debug("Try to match keys '{}' == '{}'", keyLabel, key.getKeyLabel());
                if (keyLabel.equals(key.getKeyLabel())) {
                    long sigAlg = getPKCS11Alg(marker.getAlgorithmInfo());
                    byte[] sig = key.sign(sigAlg, message);
                    response.setSignature(sig);

		    // set PIN to unauthenticated
		    setPinNotAuth(cardStateEntry);

                    return response;
                }
            }

            // TODO: use other exception
            String msg = String.format("The given DIDName %s references an unknown key.", didName);
            throw new IncorrectParameterException(msg);

	} catch (ECardException e) {
	    LOG.debug(e.getMessage(), e);
            response.setResult(e.getResult());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
            response.setResult(WSHelper.makeResult(e));
        }

        return response;
    }

    private long getPKCS11Alg(AlgorithmInfoType algInfo) throws UnsupportedAlgorithmException {
        String algUri = algInfo.getAlgorithmIdentifier().getAlgorithm();
	algUri = StringUtils.nullToEmpty(algUri);
	SignatureAlgorithms a = SignatureAlgorithms.fromAlgId(algUri);
	return a.getPkcs11Mechanism();
    }

    @Override
    public VerifySignatureResponse verifySignature(VerifySignature parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DIDListResponse didList(DIDList parameters) {
        throw new UnsupportedOperationException("Not supported, should be handled by main SAL.");
    }

    @Override
    public DIDCreateResponse didCreate(DIDCreate parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DIDGetResponse didGet(DIDGet parameters) {
        throw new UnsupportedOperationException("Not supported, should be handled by main SAL.");
    }

    @Override
    public DIDUpdateResponse didUpdate(DIDUpdate request) {
        DIDUpdateResponse response = WSHelper.makeResponse(DIDUpdateResponse.class, WSHelper.makeResultOK());

        try {
            ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
            byte[] application = cardStateEntry.getImplicitlySelectedApplicationIdentifier();
            DIDUpdateDataType didUpdateData = request.getDIDUpdateData();
            Assert.assertIncorrectParameter(didUpdateData, "The parameter DIDUpdateData is empty.");

            String didName = SALUtils.getDIDName(request);
            DIDStructureType didStruct = cardStateEntry.getDIDStructure(didName, application);
            if (didStruct == null) {
                String msg = String.format("DID %s does not exist.", didName);
                throw new NamedEntityNotFoundException(msg);
            }

	    Result updateResult;
            String protocolURI = didUpdateData.getProtocol();
	    if ("urn:oid:1.3.162.15480.3.0.9".equals(protocolURI)) {
		updateResult = updatePin(didUpdateData, cardStateEntry, didStruct);
	    } else {
                String msg = String.format("Protocol %s is not supported by this SAL.", protocolURI);
                throw new UnknownProtocolException(msg);
            }

            // create did authenticate response
            response.setResult(updateResult);

        } catch (ECardException e) {
            response.setResult(e.getResult());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
            response.setResult(WSHelper.makeResult(e));
        }

        return response;
    }

    private Result updatePin(DIDUpdateDataType didUpdateData, CardStateEntry cardStateEntry, DIDStructureType didStruct) {
	// make sure the pin is not entered already
	setPinNotAuth(cardStateEntry);

	ConnectionHandleType connectionHandle = cardStateEntry.handleCopy();
	MwSession session = managedSessions.get(connectionHandle.getSlotHandle());
	boolean protectedAuthPath = connectionHandle.getSlotInfo().isProtectedAuthPath();

	try {
	    PinChangeDialog dialog = new PinChangeDialog(gui, protectedAuthPath, session);
	    dialog.show();
	} catch (CryptokiException ex) {
	    return WSHelper.makeResultUnknownError(ex.getMessage());
	}

	return WSHelper.makeResultOK();
    }

    @Override
    public DIDDeleteResponse didDelete(DIDDelete parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate request) {
        DIDAuthenticateResponse response = WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultOK());

        try {
            ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    connectionHandle = cardStateEntry.handleCopy();
            byte[] application = cardStateEntry.getImplicitlySelectedApplicationIdentifier();
            byte[] slotHandle = connectionHandle.getSlotHandle();
            DIDAuthenticationDataType didAuthenticationData = request.getAuthenticationProtocolData();
            Assert.assertIncorrectParameter(didAuthenticationData, "The parameter AuthenticationProtocolData is empty.");

            String didName = SALUtils.getDIDName(request);
            DIDStructureType didStruct = cardStateEntry.getDIDStructure(didName, application);
            if (didStruct == null) {
                String msg = String.format("DID %s does not exist.", didName);
                throw new NamedEntityNotFoundException(msg);
            }
	    PINCompareMarkerType pinCompareMarker = new PINCompareMarkerType(didStruct.getDIDMarker());

            String protocolURI = didAuthenticationData.getProtocol();
            if (! "urn:oid:1.3.162.15480.3.0.9".equals(protocolURI)) {
                String msg = String.format("Protocol %s is not supported by this SAL.", protocolURI);
                throw new UnknownProtocolException(msg);
            }
            PINCompareDIDAuthenticateInputType pinCompareInput = new PINCompareDIDAuthenticateInputType(didAuthenticationData);
            PINCompareDIDAuthenticateOutputType pinCompareOutput = pinCompareInput.getOutputType();

            // extract pin value from auth data
            char[] pinValue = pinCompareInput.getPIN();
	    pinCompareInput.setPIN(null);

            MwSession session = managedSessions.get(slotHandle);
	    boolean protectedAuthPath = connectionHandle.getSlotInfo().isProtectedAuthPath();
	    boolean pinAuthenticated;
	    boolean pinBlocked = false;
	    if (! (pinValue == null || pinValue.length == 0) && ! protectedAuthPath) {
		// we don't need a GUI if the PIN is known
		try {
		    session.login(UserType.User, pinValue);
		} finally {
		    Arrays.fill(pinValue, ' ');
		}
		pinAuthenticated = true;
		// TODO: display error GUI if the PIN entry failed
	    } else {
		// omit GUI when Middleware has its own PIN dialog for class 2 readers
		if (protectedAuthPath && builtinPinDialog) {
		    session.loginExternal(UserType.User);
		    pinAuthenticated = true;
		} else {
		    PinEntryDialog dialog = new PinEntryDialog(gui, protectedAuthPath, pinCompareMarker, session);
		    dialog.show();
		    pinAuthenticated = dialog.isPinAuthenticated();
		    pinBlocked = dialog.isPinBlocked();
		}
	    }

	    if (pinAuthenticated) {
		cardStateEntry.addAuthenticated(didName, application);
	    } else if (pinBlocked) {
		String msg = "PIN is blocked.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORD_BLOCKED, msg);
		response.setResult(r);
	    } else {
		String msg = "Failed to enter PIN.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.CANCELLATION_BY_USER, msg);
		response.setResult(r);
	    }

            // create did authenticate response
            response.setAuthenticationProtocolData(pinCompareOutput.getAuthDataType());

	} catch (PinBlockedException ex) {
	    // TODO: set retry counter
	    String minor = ECardConstants.Minor.IFD.PASSWORD_BLOCKED;
	    Result r = WSHelper.makeResultError(minor, ex.getMessage());
	    response.setResult(r);
	} catch (PinIncorrectException ex) {
	    // TODO: set retry counter
	    String minor = ECardConstants.Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED;
	    Result r = WSHelper.makeResultError(minor, ex.getMessage());
	    response.setResult(r);
        } catch (ECardException e) {
            response.setResult(e.getResult());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
            response.setResult(WSHelper.makeResult(e));
        }

        return response;
    }

    @Override
    public ACLListResponse aclList(ACLList parameters) {
        throw new UnsupportedOperationException("Not supported, should be handled by main SAL.");
    }

    @Override
    public ACLModifyResponse aclModify(ACLModify parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void setPinNotAuth(CardStateEntry cardStateEntry) {
	LOG.info("Logout card session.");

	// TODO: implement actual state checking
	// This method only works in a avery limited way. All PIN DIDs get status unauth here.
	for (DIDInfoType didInfo : Collections.unmodifiableCollection(cardStateEntry.getAuthenticatedDIDs())) {
	    if ("urn:oid:1.3.162.15480.3.0.9".equals(didInfo.getDifferentialIdentity().getDIDProtocol())) {
		cardStateEntry.removeAuthenticated(didInfo);
	    }
	}

	// logout from session, or middleware doesn't hear the shot
	try {
	    MwSession session = managedSessions.get(cardStateEntry.handleCopy().getSlotHandle());
	    session.logout();
	} catch (CryptokiException ex) {
	    LOG.info("Failed to logout from card.");
	}
    }

    private void throwThreadKillException(Exception ex) {
	Throwable cause;
	if (ex instanceof InvocationTargetExceptionUnchecked) {
	    cause = ex.getCause();
	} else {
	    cause = ex;
	}

	if (cause instanceof ThreadTerminateException) {
	    throw (RuntimeException) cause;
	} else if (cause instanceof InterruptedException) {
	    throw new ThreadTerminateException("Thread running inside SAL interrupted.", cause);
	} else if (cause instanceof RuntimeException) {
	    throw (RuntimeException) ex;
	}
    }

}
