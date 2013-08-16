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

package org.openecard.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationStateType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.openecard.addon.sal.SALProtocol;
import org.openecard.common.WSHelper;
import org.openecard.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.sal.state.cif.DIDInfoWrapper;
import org.openecard.common.sal.state.cif.DataSetInfoWrapper;
import org.openecard.common.util.ByteArrayWrapper;


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
    private Map<String, SALProtocol> protoObjects = new TreeMap<String, SALProtocol>();

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
	return WSHelper.copyHandle(handle);
    }

    public CardApplicationPathType pathCopy() {
	return WSHelper.copyPath(handle);
    }


    public boolean hasSlotIdx() {
	return handle.getSlotIndex() != null;
    }

    public boolean matchSlotIdx(BigInteger idx) {
	BigInteger otherIdx = handle.getSlotIndex();
	if (idx != null && otherIdx != null && otherIdx.equals(idx)) {
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


    public SALProtocol setProtocol(String type, SALProtocol proto) {
	protoObjects.put(type, proto);
	return proto;
    }

    public SALProtocol getProtocol(String type) {
	return protoObjects.get(type);
    }

    public void removeProtocol(String type) {
	protoObjects.remove(type);
    }

    public void removeAllProtocols() {
	protoObjects.clear();
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
	if (didStructure != null) {
	    didStructure.setAuthenticated(this.isAuthenticated(didName, cardApplication));
	}
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
	    if(securityCondition.isAlways()) {
		return true;
	    }
	} catch (NullPointerException e){
	    // ignore
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
	if (applicationIdentifier == null) {
	    applicationIdentifier = infoObject.getImplicitlySelectedApplication();
	}
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

    public final byte[] getImplicitlySelectedApplicationIdentifier() {
	return this.infoObject.getImplicitlySelectedApplication();
    }


    ///
    /// needed to sort these entries in sets
    ///

    @Override
    public int compareTo(CardStateEntry o) {
	return this.serialNumber - o.serialNumber;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof CardStateEntry) {
	    CardStateEntry other = (CardStateEntry) obj;
	    return this == obj || this.serialNumber == other.serialNumber;
	}
	return super.equals(obj);
    }

    @Override
    public int hashCode() {
	int hash = 3;
	hash = 53 * hash + this.serialNumber;
	return hash;
    }

}
