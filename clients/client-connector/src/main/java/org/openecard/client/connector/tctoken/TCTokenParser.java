/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.connector.tctoken;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openecard.client.connector.common.ConnectorConstants;
import org.openecard.client.connector.io.LimitedInputStream;


/**
 * Implements a parser for TCTokens.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenParser {

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
	} catch (Exception ex) {
	    Logger.getLogger(TCTokenParser.class.getName()).log(Level.SEVERE, "Exception", ex);
	}
    }

    /**
     * Parse TCTokens from the input stream.
     *
     * @param data Data
     * @return List of TCTokens
     * @throws TCTokenException
     */
    public List<TCToken> parse(String data) throws TCTokenException {
	return parse(new ByteArrayInputStream(data.getBytes()));
    }

    /**
     * Parse TCTokens from the input stream.
     *
     * @param inputStream Input stream
     * @return List of TCTokens
     * @throws TCTokenException
     */
    public List<TCToken> parse(InputStream inputStream) throws TCTokenException {
	try {
	    // Parse TCTokens
	    SAXParser saxParser = saxFactory.newSAXParser();
	    LimitedInputStream stream = new LimitedInputStream(inputStream);
	    saxParser.parse(stream, saxHandler);

	    // Get TCTokens
	    List<TCToken> tokens = saxHandler.getTCTokens();

	    return tokens;
	} catch (Exception e) {
	    String message = ConnectorConstants.ConnectorError.TC_TOKEN_REFUSED.toString();
	    Logger.getLogger(TCTokenParser.class.getName()).log(Level.SEVERE, message, e);
	    throw new TCTokenException(message, e);
	}
    }

}
