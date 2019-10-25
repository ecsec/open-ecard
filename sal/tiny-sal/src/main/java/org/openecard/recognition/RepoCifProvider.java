/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.recognition;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.io.InputStream;
import org.openecard.common.interfaces.CIFProvider;
import org.openecard.common.interfaces.CardRecognition;

/**
 *
 * @author Tobias Wich
 */
public class RepoCifProvider implements CIFProvider {

    private final CardRecognition recognition;

    public RepoCifProvider(CardRecognition recognition) {
	this.recognition = recognition;
    }

    @Override
    public CardInfoType getCardInfo(ConnectionHandleType handle, String cardType) throws RuntimeException {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CardInfoType getCardInfo(String cardType) throws RuntimeException {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InputStream getCardImage(String cardType) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean needsRecognition(byte[] atr) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
