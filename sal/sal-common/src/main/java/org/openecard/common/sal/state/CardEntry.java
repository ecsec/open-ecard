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
import java.math.BigInteger;
import java.util.Arrays;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich
 */
public class CardEntry implements Comparable<CardEntry> {

    protected byte[] ctxHandle;
    protected String ifdName;
    protected BigInteger slotIdx;
    protected CardInfoWrapper cif;

    public CardEntry(byte[] ctxHandle, String ifdName, BigInteger slotIdx, CardInfoWrapper cif) {
	this.ctxHandle = ByteUtils.clone(ctxHandle);
	this.ifdName = ifdName;
	this.slotIdx = slotIdx;
	this.cif = new CardInfoWrapper(cif);
    }

    public boolean matches(byte[] ctxHandle, String ifdName, BigInteger slotIdx) {
	if (! matchesContextHandle(ctxHandle)) {
	    return false;
	}
	if (! this.ifdName.equals(ifdName)) {
	    return false;
	}
	return this.slotIdx.equals(slotIdx);
    }

    @Override
    public int compareTo(CardEntry o) {
	if (this == o) {
	    return 0;
	} else {
	    int ctxComp = Arrays.compare(this.ctxHandle, o.ctxHandle);
	    if (ctxComp != 0) {
		return ctxComp;
	    }
	    int ifdComp = this.ifdName.compareTo(o.ifdName);
	    if (ifdComp != 0) {
		return ifdComp;
	    }
	    int idxComp = this.slotIdx.compareTo(o.slotIdx);
	    return idxComp;
	}
    }

    public byte[] getCtxHandle() {
	return ctxHandle;
    }

    public String getIfdName() {
	return ifdName;
    }

    public BigInteger getSlotIdx() {
	return slotIdx;
    }

    public CardInfoWrapper getCif() {
	return cif;
    }

    public byte[] getCardApplication() {
	return null;
    }

    public boolean matchesContextHandle(byte[] ctxHandle) {
	return Arrays.equals(this.ctxHandle, ctxHandle);
    }

    public ConnectionHandleType copyHandle() {
	ConnectionHandleType connectionHandle = new ConnectionHandleType();
	this.fillConnectionHandle(connectionHandle);
	return connectionHandle;
    }

    public void fillConnectionHandle(ConnectionHandleType connectionHandle) {
	connectionHandle.setSlotIndex(slotIdx);
	connectionHandle.setIFDName(ifdName);
	connectionHandle.setContextHandle(ByteUtils.clone(ctxHandle));
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("CardEntry={");
	this.toString(builder);
	builder.append("}");
	return builder.toString();
    }

    protected void toString(StringBuilder builder) {
	builder.append("ctxHandle=");
	builder.append(ByteUtils.toHexString(ctxHandle));
	builder.append(", ifdName=");
	builder.append(ifdName);
	builder.append(", slotIdx=");
	builder.append(slotIdx);
    }

}
