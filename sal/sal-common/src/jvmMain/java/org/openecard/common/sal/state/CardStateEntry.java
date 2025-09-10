/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.openecard.addon.sal.SALProtocol;
import org.openecard.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.tlv.iso7816.FCP;
import org.openecard.common.util.HandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class CardStateEntry implements Comparable<CardStateEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(CardStateEntry.class);

    // this number is used as an number authority, so each entry can have a distinct number
    private static int numberRegistry = 0;


    private synchronized static int nextNumber() {
	return numberRegistry++;
    }

    private final int serialNumber;
    private final Set<DIDInfoType> authenticatedDIDs = new HashSet<>();

    private final ConnectionHandleType handle;

    private final CardInfoWrapper infoObject;
    private final Map<String, SALProtocol> protoObjects = new TreeMap<>();
    private FCP lastSelectedEfFCP;

    public CardStateEntry(ConnectionHandleType handle, CardInfoType cif, String interfaceProtocol) {
	this(handle, new CardInfoWrapper(cif, interfaceProtocol));
    }

    private CardStateEntry(ConnectionHandleType handle, CardInfoWrapper cifWrapper) {
	serialNumber = nextNumber();
	infoObject = new CardInfoWrapper(cifWrapper);
	this.handle = handle;
	this.handle.setCardApplication(getImplicitlySelectedApplicationIdentifier());
    }

    public CardStateEntry derive(ConnectionHandleType handle) {
	return new CardStateEntry(handle, infoObject);
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

    public void addAuthenticated(String didName, byte[] cardApplication) {
	this.authenticatedDIDs.add(this.infoObject.getDIDInfo(didName, cardApplication));
    }

    public void removeAuthenticated(DIDInfoType didInfo) {
	this.authenticatedDIDs.remove(didInfo);
    }

    public ConnectionHandleType handleCopy() {
	return HandlerUtils.copyHandle(handle);
    }

    public CardApplicationPathType pathCopy() {
	return HandlerUtils.copyPath(handle);
    }


    public boolean hasSlotIdx() {
	return handle.getSlotIndex() != null;
    }

    public boolean matchSlotIdx(BigInteger idx) {
	BigInteger otherIdx = handle.getSlotIndex();
	if (idx != null && otherIdx != null) {
	    return otherIdx.equals(idx);
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

    public Collection<SALProtocol> removeAllProtocols() {
	LOG.debug("Removing {} protocols from card state entry.", protoObjects.size());
	Collection<SALProtocol> ps = new ArrayList<>(protoObjects.values());
	protoObjects.clear();
	return ps;
    }

    public void setFCPOfSelectedEF(FCP fcp) {
	lastSelectedEfFCP = fcp;
    }
    public void unsetFCPOfSelectedEF() {
	lastSelectedEfFCP = null;
    }

    public FCP getFCPOfSelectedEF() {
	return lastSelectedEfFCP;
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
	    didStructure.setAuthenticated(CardStateEntry.isAuthenticated(infoObject, didName, cardApplication, authenticatedDIDs));
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
	DIDStructureType didStructure = this.infoObject.getDIDStructure(didName, didScope);
	if (didStructure != null) {
	    didStructure.setAuthenticated(CardStateEntry.isAuthenticated(infoObject, didName, didScope, authenticatedDIDs));
	}
	return didStructure;
    }

    public static boolean isAuthenticated(CardInfoWrapper infoObject, String didName, byte[] cardApplication, Set<DIDInfoType> authenticatedDIDs) {
	DIDInfoType didInfo = infoObject.getDIDInfo(didName, cardApplication);
	return authenticatedDIDs.contains(didInfo);
    }

    public static boolean isAuthenticated(CardInfoWrapper infoObject, String didName, DIDScopeType didScope, Set<DIDInfoType> authenticatedDIDs) {
	DIDInfoType didInfo = infoObject.getDIDInfo(didName, didScope);
	return authenticatedDIDs.contains(didInfo);
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

    @Override
    public String toString() {
	Formatter f = new Formatter();
	f.format("CardStateEntry@%d {%n", serialNumber);
	if (handle != null) {
	    f.format("  handle=%s%n", HandlerUtils.print(handle, "  ", "  "));
	}
	for (String proto : protoObjects.keySet()) {
	    f.format("  protocol=%s%n", proto);
	}
	for (DIDInfoType did : authenticatedDIDs) {
	    f.format("  authDid=%s%n", did.getId());
	}
	f.format("}");

	return f.toString();
    }

}
