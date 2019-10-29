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
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


/**
 * Implements a parser for TCTokens.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class TCTokenParser {

    private static final Logger LOG = LoggerFactory.getLogger(TCTokenParser.class);
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
	    LOG.warn(ex.getMessage(), ex);
	    // Android doesn't support the corresponding xml feature
	    // TODO: translate when exception changes
	    //throw new IllegalArgumentException(lang.getOriginalMessage(UNSUPPORTED_FEATURE), ex);
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
	    XMLReader reader = saxParser.getXMLReader();
	    reader.setContentHandler(saxHandler);
	    LimitedInputStream stream = new LimitedInputStream(inputStream);
	    reader.parse(new InputSource(stream));

	    // Get TCTokens
	    List<TCToken> tokens = saxHandler.getTCTokens();

	    return tokens;
	} catch (ParserConfigurationException | SAXException | IOException ex) {
	    LOG.error(ex.getMessage(), ex);
	    throw new InvalidTCTokenException(MALFORMED_TOKEN, ex);
	}
    }

}
