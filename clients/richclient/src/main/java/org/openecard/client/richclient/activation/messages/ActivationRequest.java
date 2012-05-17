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
package org.openecard.client.richclient.activation.messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import org.openecard.client.richclient.activation.ActivationException;
import org.openecard.client.richclient.activation.common.ActivationConstants.ActivationError;
import org.openecard.client.richclient.activation.io.LimitedInputStream;
import org.openecard.client.richclient.activation.tctoken.TCToken;
import org.openecard.client.richclient.activation.tctoken.TCTokenConverter;
import org.openecard.client.richclient.activation.tctoken.TCTokenException;
import org.openecard.client.richclient.activation.tctoken.TCTokenGrabber;
import org.openecard.client.richclient.activation.tctoken.TCTokenParser;
import org.openecard.client.richclient.activation.tctoken.TCTokenVerifier;


/**
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ActivationRequest {

    private URI tokenURI;
    private HashMap<String, String> parameters = new HashMap<String, String>();
    private List<TCToken> tokens;
    private InputStream input;

    /**
     * Create a new ActivationRequest.
     *
     * @param input InputStream
     */
    public ActivationRequest(InputStream input) {
	this.input = input;
    }

    /**
     * Handles the activation request.
     *
     * @throws Exception
     */
    public void handleRequest() throws Exception {
	try {
	    LimitedInputStream socketStream = new LimitedInputStream(input);
	    InputStreamReader streamReader = new InputStreamReader(socketStream);
	    BufferedReader reader = new BufferedReader(streamReader);
	    String line = reader.readLine();

	    if (line.startsWith("GET")) {
		if (line.contains("eID-Client?tcTokenURL")) {
		    parseURI(line);
		    // Not needed yet.
                    while (!(line = reader.readLine()).isEmpty()) {
//                        parseParameter(line);
			System.out.println(line);
                    }
		    parseTCToken();
		} else {
		    throw new IllegalArgumentException();
		}
	    } else {
		throw new IllegalArgumentException();
	    }
	} catch (TCTokenException e) {
	    throw e;
	} catch (IllegalArgumentException e) {
	    String message = ActivationError.BAD_REQUEST.toString();
	    throw new ActivationException(message, e);
	} catch (IOException e) {
	    String message = ActivationError.BAD_REQUEST.toString();
	    throw new ActivationException(message, e);
	} catch (Throwable e) {
	    String message = ActivationError.INTERNAL_ERROR.toString();
	    throw new ActivationException(message, e);
	}
    }

    /**
     * Parses the URI from the HTTP GET request.
     *
     * @param input Input
     */
    private void parseURI(String input) {
	try {
	    String uri = input.split(" ")[1];
	    uri = uri.substring(uri.indexOf("=") + 1);
	    tokenURI = new URI(uri);
	} catch (Exception e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /**
     * Parses the parameter of the HTTP request.
     *
     * @param input Input
     */
    private void parseParameter(String input) {
	try {
	    int x = input.indexOf(":");
	    String parameter = input.substring(0, x);
	    String value = input.substring(x + 1, input.length());
	    if (value.startsWith(" ")) {
		value = value.substring(1, value.length());
	    }

	    parameters.put(parameter, value);
	} catch (Exception e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /**
     * Parses the TCToken.
     *
     * @throws TCTokenException
     */
    private void parseTCToken() throws TCTokenException {
	// Get TCToken from the given url
	TCTokenGrabber grabber = new TCTokenGrabber();
	String data = grabber.getResource(tokenURI.toString());

	//FIXME Remove me
	TCTokenConverter converter = new TCTokenConverter();
	data = converter.convert(data);

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new TCTokenException(ActivationError.TC_TOKEN_NOT_AVAILABLE.toString());
	}

	// Verify the TCToken
	TCTokenVerifier ver = new TCTokenVerifier(tokens);
	ver.verify();
    }

    /**
     * Returns a list of TCTokens.
     *
     * @return TCTokens
     */
    public List<TCToken> getTCTokens() {
	return tokens;
    }

}
