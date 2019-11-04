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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import java.util.HashSet;
import java.util.Set;
import org.openecard.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.common.tlv.iso7816.FCP;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich
 */
public class ConnectedCardEntry extends CardEntry {

    protected final byte[] slotHandle;
    protected final byte[] cardApplication;
    private final Set<DIDInfoType> authenticatedDIDs = new HashSet<>();
    private FCP lastSelectedEfFCP;

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

    public void setFCPOfSelectedEF(FCP fcp) {
	lastSelectedEfFCP = fcp;
    }
    public void unsetFCPOfSelectedEF() {
	lastSelectedEfFCP = null;
    }

    public FCP getFCPOfSelectedEF() {
	return lastSelectedEfFCP;
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
	    didStructure.setAuthenticated(CardStateEntry.isAuthenticated(cif, didName, cardApplication, authenticatedDIDs));
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
	    didStructure.setAuthenticated(CardStateEntry.isAuthenticated(cif, didName, didScope, authenticatedDIDs));
	}
	return didStructure;
    }

    public CardApplicationWrapper getCurrentCardApplication() {
	return this.cif.getCardApplication(this.cardApplication);
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
