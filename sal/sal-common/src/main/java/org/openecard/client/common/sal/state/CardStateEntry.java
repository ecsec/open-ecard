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

package org.openecard.client.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.datatype.XMLGregorianCalendar;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.client.common.sal.state.cif.CardInfoWrapper;
import org.openecard.client.common.sal.state.cif.DIDInfoWrapper;
import org.openecard.client.common.sal.state.cif.DataSetInfoWrapper;
import org.openecard.client.common.util.ByteArrayWrapper;
import org.openecard.client.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardStateEntry implements Comparable<CardStateEntry> {

    // this number is used as an number authority, so each entry can have a distinct number
    private static int numberRegistry = 0;


    private synchronized static int nextNumber() {
	return numberRegistry++;
    }

    private final int serialNumber;
    private Set<DIDInfoType> authenticatedDIDs = new HashSet<DIDInfoType>();

    private final ConnectionHandleType handle;

    private final CardInfoWrapper infoObject;
    private Map<String, Protocol> protoObjects = new TreeMap<String, Protocol>();

    public CardStateEntry(ConnectionHandleType handle, CardInfoType cif) {
	serialNumber = nextNumber();
	infoObject = new CardInfoWrapper(cif);
	this.handle = handle;
	this.handle.setCardApplication(getImplicitlySelectedApplicationIdentifier());
    }


    public String getCardType() {
	return infoObject.getCardType();
    }


    public void setCurrentCardApplication(byte[] currentCardApplication) {
	this.handle.setCardApplication(currentCardApplication);
    }

    public CardApplicationWrapper getCurrentCardApplication() {
	return infoObject.getCardApplication(this.handle.getCardApplication());
    }


    public Set<DIDInfoType> getAuthenticatedDIDs() {
	return authenticatedDIDs;
    }

    public void addAuthenticated(String didName, byte[] cardApplication){
	this.authenticatedDIDs.add(this.infoObject.getDIDInfo(didName, cardApplication));
    }

    public void removeAuthenticated(DIDInfoType didInfo){
	this.authenticatedDIDs.remove(didInfo);
    }

    public ConnectionHandleType handleCopy() {
	ConnectionHandleType result = new ConnectionHandleType();
	copyPath(result, handle);
	result.setSlotHandle(ByteUtils.clone(handle.getSlotHandle()));
	result.setRecognitionInfo(copyRecognition(handle.getRecognitionInfo()));
	return result;
    }

    public CardApplicationPathType pathCopy() {
	CardApplicationPathType result = new CardApplicationPathType();
	copyPath(result, handle);
	return result;
    }

    private static void copyPath(CardApplicationPathType out, CardApplicationPathType in) {
	out.setCardApplication(ByteUtils.clone(in.getCardApplication()));
	out.setChannelHandle(copyChannel(in.getChannelHandle()));
	out.setContextHandle(ByteUtils.clone(in.getContextHandle()));
	out.setIFDName(in.getIFDName());
	out.setSlotIndex(in.getSlotIndex()); // TODO: copy bigint
    }

    private static ChannelHandleType copyChannel(ChannelHandleType handle) {
	if (handle == null) {
	    return null;
	}
	ChannelHandleType result = new ChannelHandleType();
	result.setBinding(handle.getBinding());
	result.setPathSecurity(copyPathSec(handle.getPathSecurity()));
	result.setProtocolTerminationPoint(handle.getProtocolTerminationPoint());
	result.setSessionIdentifier(handle.getSessionIdentifier());
	return result;
    }

    private static ConnectionHandleType.RecognitionInfo copyRecognition(ConnectionHandleType.RecognitionInfo rec) {
	if (rec == null) {
	    return null;
	}
	ConnectionHandleType.RecognitionInfo result = new ConnectionHandleType.RecognitionInfo();
	if (rec.getCaptureTime() != null) {
	    result.setCaptureTime((XMLGregorianCalendar)rec.getCaptureTime().clone());
	}
	result.setCardIdentifier(ByteUtils.clone(rec.getCardIdentifier()));
	result.setCardType(rec.getCardType());
	return result;
    }

    private static PathSecurityType copyPathSec(PathSecurityType sec) {
	if (sec == null) {
	    return null;
	}
	PathSecurityType result = new PathSecurityType();
	result.setParameters(sec.getParameters()); // TODO: copy depending on actual content
	result.setProtocol(sec.getProtocol());
	return result;
    }


    public boolean hasSlotIdx() {
	return handle.getSlotIndex() != null;
    }

    public boolean matchSlotIdx(BigInteger idx) {
	BigInteger otherIdx = handle.getSlotIndex();
	if (otherIdx != null && otherIdx != null && otherIdx.equals(idx)) {
	    return true;
	}
	return false;
    }

    public String getIfdName() {
	return handle.getIFDName();
    }


    public CardInfoWrapper getInfo() {
	return infoObject;
    }


    public void setSlotHandle(byte[] slotHandle){
	this.handle.setSlotHandle(slotHandle);
    }


    public Protocol setProtocol(String type, Protocol proto) {
	protoObjects.put(type, proto);
	return proto;
    }

    public Protocol getProtocol(String type) {
	return protoObjects.get(type);
    }

    public void removeProtocol(String type) {
	protoObjects.remove(type);
    }


    /**
     *
     * @param didName Name of the DID
     * @param cardApplication Identifier of the cardapplication
     * @return DIDStructure for the specified didName and cardapplication or null,
     *         if no such did exists.
     */
    public DIDStructureType getDIDStructure(String didName, byte[] cardApplication) {
	DIDStructureType didStructure = this.infoObject.getDIDStructure(didName, cardApplication);
	didStructure.setAuthenticated(this.isAuthenticated(didName, cardApplication));
	return didStructure;
    }

    public boolean isAuthenticated(String didName, byte[] cardApplication) {
	if (this.getAuthenticatedDIDs().contains(this.infoObject.getDIDInfo(didName, cardApplication))) {
	    return true;
	} else {
	    return false;
	}
    }

    private boolean checkSecurityCondition(SecurityConditionType securityCondition) {
	byte[] cardApplication = infoObject.getImplicitlySelectedApplication();
	try{
	    if(securityCondition.isAlways())
		return true;
	} catch (NullPointerException e){
	    /* ignore */
	}
	if (securityCondition.getDIDAuthentication()!=null) {
	    DIDAuthenticationStateType didAuthenticationState = securityCondition.getDIDAuthentication();
	    if (didAuthenticationState.isDIDState()) {
		return isAuthenticated(didAuthenticationState.getDIDName(), cardApplication);
	    } else {
		return !isAuthenticated(didAuthenticationState.getDIDName(), cardApplication);
	    }
	} else if (securityCondition.getOr() != null) {
	    for (SecurityConditionType securityConditionOR : securityCondition.getOr().getSecurityCondition()) {
		if (checkSecurityCondition(securityConditionOR)) {
		    return true;
		}
	    }
	} else if (securityCondition.getAnd() != null) {
	    for(SecurityConditionType securityConditionAND : securityCondition.getAnd().getSecurityCondition()) {
		if (!checkSecurityCondition(securityConditionAND)) {
		    return false;
		}
	    }
	    return true;
	} else if (securityCondition.getNot() != null) {
	    return !checkSecurityCondition(securityCondition.getNot());
	}

	return false;
    }

    public boolean checkDIDSecurityCondition(byte[] cardApplication, String didName, Enum<?> serviceAction) {
	CardApplicationWrapper application = this.infoObject.getCardApplications().get(new ByteArrayWrapper(cardApplication));
	DIDInfoWrapper dataSetInfo = application.getDIDInfo(didName);
	SecurityConditionType securityCondition = dataSetInfo.getSecurityCondition(serviceAction);
	if (securityCondition != null) {
	    return checkSecurityCondition(securityCondition);
	} else {
	    return false;
	}
    }

    public boolean checkApplicationSecurityCondition(byte[] applicationIdentifier, Enum<?> serviceAction) {
	CardApplicationWrapper application = this.infoObject.getCardApplications().get(new ByteArrayWrapper(applicationIdentifier));
	SecurityConditionType securityCondition = application.getSecurityCondition(serviceAction);
	if (securityCondition != null) {
	    return checkSecurityCondition(securityCondition);
	} else {
	    return false;
	}
    }

    public boolean checkDataSetSecurityCondition(byte[] cardApplication, String dataSetName, Enum<?> serviceAction) {
	CardApplicationWrapper application = this.infoObject.getCardApplications().get(new ByteArrayWrapper(cardApplication));
	DataSetInfoWrapper dataSetInfo = application.getDataSetInfo(dataSetName);
	SecurityConditionType securityCondition = dataSetInfo.getSecurityCondition(serviceAction);
	if (securityCondition != null) {
	    return checkSecurityCondition(securityCondition);
	} else {
	    return false;
	}
    }

    public byte[] getImplicitlySelectedApplicationIdentifier(){
	return this.infoObject.getImplicitlySelectedApplication();
    }


    ///
    /// needed to sort these entries in sets
    ///

    @Override
    public int compareTo(CardStateEntry o) {
	return this.serialNumber - o.serialNumber;
    }

}
