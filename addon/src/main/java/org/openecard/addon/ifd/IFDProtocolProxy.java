/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.addon.ifd;

import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import org.openecard.addon.AbstractFactory;
import org.openecard.addon.Context;
import org.openecard.addon.ActionInitializationException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class IFDProtocolProxy extends AbstractFactory<IFDProtocol> implements IFDProtocol {

    private IFDProtocol c;

    public IFDProtocolProxy(String protocolClass, ClassLoader classLoader) {
	super(protocolClass, classLoader);
    }

    @Override
    public EstablishChannelResponse establish(EstablishChannel req) {
	return c.establish(req);
    }

    @Override
    public byte[] applySM(byte[] commandAPDU) {
	return c.applySM(commandAPDU);
    }

    @Override
    public byte[] removeSM(byte[] responseAPDU) {
	return c.removeSM(responseAPDU);
    }

    @Override
    public void init(Context aCtx) throws ActionInitializationException {
	c = loadInstance(aCtx, IFDProtocol.class);
    }

    @Override
    public void destroy() {
	c.destroy();
    }

}
