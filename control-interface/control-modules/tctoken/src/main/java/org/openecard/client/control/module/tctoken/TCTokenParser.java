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

package org.openecard.client.control.module.tctoken;

import generated.TCTokenType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openecard.client.common.io.LimitedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a parser for TCTokens.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenParser {

    private static final Logger _logger = LoggerFactory.getLogger(TCTokenParser.class);
    private SAXParserFactory saxFactory;
    private TCTokenSAXHandler saxHandler;

    /**
     * Creates a new parser for TCTokens.
     */
    public TCTokenParser() {
	saxFactory = SAXParserFactory.newInstance();
	saxHandler = new TCTokenSAXHandler();

	try {
	    saxFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	} catch (Exception e) {
	    _logger.error("Exception", e);
	}
    }

    /**
     * Parse TCTokens from the input stream.
     *
     * @param data Data
     * @return List of TCTokens
     * @throws TCTokenException
     */
    public List<TCTokenType> parse(String data) throws TCTokenException {
	return parse(new ByteArrayInputStream(data.getBytes()));
    }

    /**
     * Parse TCTokens from the input stream.
     *
     * @param inputStream Input stream
     * @return List of TCTokens
     * @throws TCTokenException
     */
    public List<TCTokenType> parse(InputStream inputStream) throws TCTokenException {
	try {
	    // Parse TCTokens
	    SAXParser saxParser = saxFactory.newSAXParser();
	    LimitedInputStream stream = new LimitedInputStream(inputStream);
	    saxParser.parse(stream, saxHandler);

	    // Get TCTokens
	    List<TCTokenType> tokens = saxHandler.getTCTokens();

	    return tokens;
	} catch (Exception e) {
	     _logger.error("Exception", e);
	    throw new TCTokenException("TCToken is malformed", e);
	}
    }

}
