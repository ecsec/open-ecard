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

package org.openecard.client.ws;

import org.openecard.client.common.GenericFactory;
import org.openecard.client.common.GenericFactoryException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WSMarshallerFactory {

    private GenericFactory<WSMarshaller> factory;

    private WSMarshallerFactory() throws WSMarshallerException {
	try {
	    String key = "org.openecard.client.ws.marshaller.impl";
	    factory = new GenericFactory<WSMarshaller>(WsdefProperties.properties(), key);
	} catch (GenericFactoryException ex) {
	    throw new WSMarshallerException(ex);
	}
    }


    private static WSMarshallerFactory inst;

    public static WSMarshaller createInstance() throws WSMarshallerException {
	if (inst == null) {
	    inst = new WSMarshallerFactory();
	}

	try {
	    return inst.factory.getInstance();
	} catch (GenericFactoryException ex) {
	    throw new WSMarshallerException(ex);
	}
    }

}
