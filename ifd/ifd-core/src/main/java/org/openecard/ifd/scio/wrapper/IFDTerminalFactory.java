/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.ifd.scio.wrapper;

import org.openecard.common.GenericFactory;
import org.openecard.common.GenericFactoryException;
import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.ifd.scio.IFDException;
import org.openecard.ifd.scio.IFDProperties;


/**
 * Class to retrieve an SCIO based TerminalFactory implementation.
 *
 * @author Tobias Wich
 */
public class IFDTerminalFactory {

    private static final String FACTORY_KEY = "org.openecard.ifd.scio.factory.impl";

    private GenericFactory<TerminalFactory> factory;

    private IFDTerminalFactory() throws IFDException {
	try {
	    factory = new GenericFactory<>(TerminalFactory.class, IFDProperties.properties(), FACTORY_KEY);
	} catch (GenericFactoryException ex) {
	    throw new IFDException(ex);
	}
    }


    private static IFDTerminalFactory factoryInst = null;

    public static synchronized TerminalFactory getInstance() throws IFDException {
	if (factoryInst == null) {
	    factoryInst = new IFDTerminalFactory();
	}

	try {
	    return factoryInst.factory.getInstance();
	} catch (GenericFactoryException ex) {
	    throw new IFDException(ex);
	}
    }

}
