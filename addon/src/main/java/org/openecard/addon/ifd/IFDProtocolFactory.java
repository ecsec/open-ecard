/****************************************************************************
 *  Copyright (C) 2013 ecsec GmbH.
 *  All rights reserved.
 *  Contact: ecsec GmbH (info@ecsec.de)
 *
 *  This file is part of SkIDentity.
 *
 *  This file may be used in accordance with the terms and conditions
 *  contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.addon.ifd;

import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import org.openecard.addon.Context;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class IFDProtocolFactory implements IFDProtocol {

    @Override
    public EstablishChannelResponse establish(EstablishChannel req) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] applySM(byte[] commandAPDU) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] removeSM(byte[] responseAPDU) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(Context aCtx) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
