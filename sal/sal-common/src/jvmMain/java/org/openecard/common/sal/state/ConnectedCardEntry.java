/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.*;

import java.util.HashSet;
import java.util.Set;
import org.openecard.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.sal.state.cif.DIDInfoWrapper;
import org.openecard.common.sal.state.cif.DataSetInfoWrapper;
import org.openecard.common.tlv.iso7816.FCP;
import org.openecard.common.util.ByteArrayWrapper;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich
 */
public class ConnectedCardEntry extends CardEntry {

    protected final byte[] slotHandle;
    protected byte[] cardApplication;
    private final Set<DIDInfoType> authenticatedDIDs = new HashSet<>();
    private FCP lastSelectedEfFCP;
	private DataSetInfoType currentFile;

    public ConnectedCardEntry(byte[] slotHandle, byte[] cardApplication, CardEntry base) {
	super(base.ctxHandle, base.ifdName, base.slotIdx, base.cif);
	this.slotHandle = ByteUtils.clone(slotHandle);
	this.cardApplication = ByteUtils.clone(cardApplication);
    }

    @Override
    public void fillConnectionHandle(ConnectionHandleType connectionHandle) {
	super.fillConnectionHandle(connectionHandle);
	connectionHandle.setSlotHandle(slotHandle);
    }

    public void setSelectedEF(String dataSetName, FCP fcp) {
	currentFile = this.cif.getDataSet(dataSetName, cardApplication);
	lastSelectedEfFCP = fcp;
    }
    public void unsetSelectedEF() {
	lastSelectedEfFCP = null;
	currentFile = null;
    }

	public DataSetInfoType getCurrentFileInfo() {
		return this.currentFile;
	}

    public FCP getFCPOfSelectedEF() {
	return lastSelectedEfFCP;
    }

    public byte[] getSlotHandle() {
	return slotHandle;
    }

    public Set<DIDInfoType> getAuthenticatedDIDs() {
	return authenticatedDIDs;
    }

    public void addAuthenticated(String didName, byte[] cardApplication) {
	this.authenticatedDIDs.add(this.cif.getDIDInfo(didName, cardApplication));
    }

    public void removeAuthenticated(DIDInfoType didInfo) {
	this.authenticatedDIDs.remove(didInfo);
    }

    /**
     *
     * @param didName Name of the DID
     * @param cardApplication Identifier of the cardapplication
     * @return DIDStructure for the specified didName and cardapplication or null,
     *         if no such did exists.
     */
    public DIDStructureType getDIDStructure(String didName, byte[] cardApplication) {
	DIDStructureType didStructure = this.cif.getDIDStructure(didName, cardApplication);
	if (didStructure != null) {
	    didStructure.setAuthenticated(isAuthenticated(cif, didName, cardApplication, authenticatedDIDs));
	}
	return didStructure;
    }

    /**
     *
     * @param didName Name of the DID
     * @param didScope Scope of the DID
     * @return DIDStructure for the specified didName and cardapplication or null,
     *         if no such did exists.
     */
    public DIDStructureType getDIDStructure(String didName, DIDScopeType didScope) {
	DIDStructureType didStructure = this.cif.getDIDStructure(didName, didScope);
	if (didStructure != null) {
	    didStructure.setAuthenticated(isAuthenticated(cif, didName, didScope, authenticatedDIDs));
	}
	return didStructure;
    }

    @Override
    public byte[] getCardApplication() {
	return this.cardApplication;
    }

    public void setCurrentCardApplication(byte[] applicationID) {
	this.cardApplication = ByteUtils.clone(applicationID);
	unsetSelectedEF();
    }
    
    public CardApplicationWrapper getCurrentCardApplication() {
	return this.cif.getCardApplication(this.cardApplication);
    }


    public boolean checkDataSetSecurityCondition(byte[] cardApplication, String dataSetName, Enum<?> serviceAction) {
	return checkDataSetSecurityCondition(this.cif, this.authenticatedDIDs, cardApplication, dataSetName, serviceAction);
    }

    public static boolean checkDataSetSecurityCondition(
	CardInfoWrapper info,
	Set<DIDInfoType> authenticatedDIDs,
	byte[] cardApplication,
	String dataSetName,
	Enum<?> serviceAction
    ) {
	CardApplicationWrapper application = info.getCardApplications().get(new ByteArrayWrapper(cardApplication));
	DataSetInfoWrapper dataSetInfo = application.getDataSetInfo(dataSetName);
	SecurityConditionType securityCondition = dataSetInfo.getSecurityCondition(serviceAction);
	if (securityCondition != null) {
	    return checkSecurityCondition(info, securityCondition, authenticatedDIDs);
	} else {
	    return false;
	}
    }

    public static boolean checkDIDSecurityCondition(
	CardInfoWrapper info,
	Set<DIDInfoType> authenticatedDIDs,
	byte[] cardApplication,
	String didName,
	Enum<?> serviceAction
    ) {
	CardApplicationWrapper application = info.getCardApplications().get(new ByteArrayWrapper(cardApplication));
	DIDInfoWrapper dataSetInfo = application.getDIDInfo(didName);
	SecurityConditionType securityCondition = dataSetInfo.getSecurityCondition(serviceAction);
	if (securityCondition != null) {
	    return checkSecurityCondition(info, securityCondition, authenticatedDIDs);
	} else {
	    return false;
	}
    }

    public boolean checkDIDSecurityCondition(byte[] cardApplication, String didName, Enum<?> serviceAction) {
	return checkDIDSecurityCondition(this.cif, this.authenticatedDIDs, cardApplication, didName, serviceAction);
    }

    public static boolean checkSecurityCondition(CardInfoWrapper infoObject, SecurityConditionType securityCondition, Set<DIDInfoType> authenticatedDIDs) {
	byte[] cardApplication;
	try{
	    if(securityCondition.isAlways()) {
		return true;
	    }
	} catch (NullPointerException e){
	    // ignore
	}
	if (securityCondition.getDIDAuthentication()!=null) {
	    DIDAuthenticationStateType didAuthenticationState = securityCondition.getDIDAuthentication();
	    cardApplication = infoObject.getApplicationIdByDidName(didAuthenticationState.getDIDName(), null);
	    if (didAuthenticationState.isDIDState()) {
		return isAuthenticated(infoObject, didAuthenticationState.getDIDName(), cardApplication, authenticatedDIDs);
	    } else {
		return !isAuthenticated(infoObject, didAuthenticationState.getDIDName(), cardApplication, authenticatedDIDs);
	    }
	} else if (securityCondition.getOr() != null) {
	    for (SecurityConditionType securityConditionOR : securityCondition.getOr().getSecurityCondition()) {
		if (checkSecurityCondition(infoObject, securityConditionOR, authenticatedDIDs)) {
		    return true;
		}
	    }
	} else if (securityCondition.getAnd() != null) {
	    for(SecurityConditionType securityConditionAND : securityCondition.getAnd().getSecurityCondition()) {
		if (!checkSecurityCondition(infoObject, securityConditionAND, authenticatedDIDs)) {
		    return false;
		}
	    }
	    return true;
	} else if (securityCondition.getNot() != null) {
	    return !checkSecurityCondition(infoObject, securityCondition.getNot(), authenticatedDIDs);
	}

	return false;
    }

    public static boolean isAuthenticated(CardInfoWrapper infoObject, String didName, byte[] cardApplication, Set<DIDInfoType> authenticatedDIDs) {
	DIDInfoType didInfo = infoObject.getDIDInfo(didName, cardApplication);
	return authenticatedDIDs.contains(didInfo);
    }

    public static boolean isAuthenticated(CardInfoWrapper infoObject, String didName, DIDScopeType didScope, Set<DIDInfoType> authenticatedDIDs) {
	DIDInfoType didInfo = infoObject.getDIDInfo(didName, didScope);
	return authenticatedDIDs.contains(didInfo);
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("ConnectedCardEntry={");
	this.toString(builder);
	builder.append("}");
	return builder.toString();
    }

    @Override
    protected void toString(StringBuilder builder) {
	builder.append("slotHandle=");
	builder.append(ByteUtils.toHexString(slotHandle));
	builder.append(", ");
	super.toString(builder);
    }

}
