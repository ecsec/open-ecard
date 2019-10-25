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
	this.slotIdx = BigInteger.ZERO;
	this.cif = new CardInfoWrapper(cif);
    }

    public boolean matches(byte[] ctxHandle, String ifdName, BigInteger slotIdx) {
	if (! Arrays.equals(this.ctxHandle, ctxHandle)) {
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

}
