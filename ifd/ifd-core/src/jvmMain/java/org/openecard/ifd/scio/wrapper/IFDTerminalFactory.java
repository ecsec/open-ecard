/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.ifd.scio.IFDException;
import org.openecard.ifd.scio.IFDProperties;
import org.openecard.ws.common.GenericFactory;
import org.openecard.ws.common.GenericFactoryException;
import org.openecard.ws.common.GenericInstanceProvider;


/**
 * Class to retrieve an SCIO based TerminalFactory implementation.
 *
 * @author Tobias Wich
 */
public class IFDTerminalFactory implements GenericInstanceProvider<TerminalFactory> {

    private static final String FACTORY_KEY = "org.openecard.ifd.scio.factory.impl";

    private final GenericInstanceProvider<TerminalFactory> factory;

    public IFDTerminalFactory(GenericInstanceProvider<TerminalFactory> factory) {
	this.factory = factory;
    }

    @Override
    public TerminalFactory getInstance() throws GenericFactoryException {
	return this.factory.getInstance();
    }

    private static IFDTerminalFactory factoryInst = null;

    private static IFDTerminalFactory createDefaultCongirationFactory() throws IFDException {
	GenericInstanceProvider<TerminalFactory> factory;
	try {
	    factory = new GenericFactory<>(TerminalFactory.class, IFDProperties.properties(), FACTORY_KEY);
	} catch (GenericFactoryException ex) {
	    throw new IFDException(ex);
	}
	return new IFDTerminalFactory(factory);
    }

    public static synchronized IFDTerminalFactory configBackedInstance() throws IFDException {
	if (factoryInst == null) {
	    factoryInst = createDefaultCongirationFactory();
	}
	return factoryInst;
    }

}
