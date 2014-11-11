/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 *
 * @author Hans-Martin Haase
 */
public class CustomErrorHandler implements ErrorHandler {

    public static final Logger logger = LoggerFactory.getLogger(CustomErrorHandler.class);

    @Override
    public void warning(SAXParseException exception) throws SAXException {
	// Ignore this. One of the TRs demands to accept as much as possible.
	logger.warn(exception.getLocalizedMessage());
	throw exception;
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
	throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
	throw exception;
    }

}
