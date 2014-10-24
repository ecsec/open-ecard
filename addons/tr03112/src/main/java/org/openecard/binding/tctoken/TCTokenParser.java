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

package org.openecard.binding.tctoken;

import org.openecard.binding.tctoken.ex.InvalidTCTokenException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openecard.common.I18n;
import org.openecard.common.io.LimitedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;


/**
 * Implements a parser for TCTokens.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class TCTokenParser {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenParser.class);
    private static final String MALFORMED_TOKEN = "invalid.tctoken.exception.malformed_tctoken";
    private static final String UNSUPPORTED_FEATURE = "illegal.argument.exception.unsupported_parser_feature";
    private final I18n lang = I18n.getTranslation("tr03112");
    private SAXParserFactory saxFactory;
    private TCTokenSAXHandler saxHandler;

    /**
     * Creates a new parser for TCTokens.
     *
     * @throws IllegalArgumentException Thrown when the parser could not be initialized with the specified parameters.
     */
    public TCTokenParser() {
	saxFactory = SAXParserFactory.newInstance();
	saxHandler = new TCTokenSAXHandler();

	try {
	    saxFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	} catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new IllegalArgumentException(lang.translationForKey(UNSUPPORTED_FEATURE), ex);
	}
    }

    /**
     * Parse TCTokens from the input stream.
     *
     * @param data Data
     * @return List of TCTokens
     * @throws InvalidTCTokenException Thrown in case the SAX parser had an error.
     */
    public List<TCToken> parse(@Nonnull String data) throws InvalidTCTokenException {
	return parse(new ByteArrayInputStream(data.getBytes()));
    }

    /**
     * Parse TCTokens from given the input stream.
     *
     * @param inputStream Input stream
     * @return List of TCTokens
     * @throws InvalidTCTokenException Thrown in case the SAX parser had an error reading the stream.
     */
    public List<TCToken> parse(@Nonnull InputStream inputStream) throws InvalidTCTokenException {
	try {
	    // Parse TCTokens
	    SAXParser saxParser = saxFactory.newSAXParser();
	    LimitedInputStream stream = new LimitedInputStream(inputStream);
	    saxParser.parse(stream, saxHandler);

	    // Get TCTokens
	    List<TCToken> tokens = saxHandler.getTCTokens();

	    return tokens;
	} catch (ParserConfigurationException | SAXException | IOException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new InvalidTCTokenException(MALFORMED_TOKEN, ex);
	}
    }

}
