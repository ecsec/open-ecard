/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.sal;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.interfaces.CIFProvider;


/**
 *
 * @author Tobias Wich
 */
public class CombinedCIFProvider implements CIFProvider {

    private final List<CIFProvider> cps;

    public CombinedCIFProvider() {
	this.cps = new ArrayList<>();
    }

    public void addCifProvider(CIFProvider cifProvider) {
	this.cps.add(cifProvider);
    }

    @Override
    public CardInfoType getCardInfo(ConnectionHandleType handle, String cardType) throws RuntimeException {
	for (CIFProvider cp : cps) {
	    try {
		CardInfoType cif = cp.getCardInfo(handle, cardType);
		if (cif != null) {
		    return cif;
		}
	    } catch (RuntimeException ex) {
		// ignore
	    }
	}
	// nothing found
	return null;
    }

    @Override
    public CardInfoType getCardInfo(String cardType) throws RuntimeException {
	for (CIFProvider cp : cps) {
	    try {
		CardInfoType cif = cp.getCardInfo(cardType);
		if (cif != null) {
		    return cif;
		}
	    } catch (RuntimeException ex) {
		// ignore
	    }
	}
	// nothing found
	return null;
    }

    @Override
    public InputStream getCardImage(String cardType) {
	for (CIFProvider cp : cps) {
	    InputStream in = cp.getCardImage(cardType);
	    if (in != null) {
		return in;
	    }
	}
	// nothing found
	return null;
    }

    @Override
    public boolean needsRecognition(byte[] atr) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
