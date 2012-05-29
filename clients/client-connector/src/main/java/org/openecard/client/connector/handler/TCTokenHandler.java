package org.openecard.client.connector.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import org.openecard.client.connector.activation.ConnectorException;
import org.openecard.client.connector.common.ConnectorConstants;
import org.openecard.client.connector.common.ErrorPage;
import org.openecard.client.connector.io.CustomStringBuilder;
import org.openecard.client.connector.messages.TCTokenRequest;
import org.openecard.client.connector.messages.TCTokenResponse;
import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;
import org.openecard.client.connector.tctoken.TCToken;
import org.openecard.client.connector.tctoken.TCTokenConverter;
import org.openecard.client.connector.tctoken.TCTokenException;
import org.openecard.client.connector.tctoken.TCTokenGrabber;
import org.openecard.client.connector.tctoken.TCTokenParser;
import org.openecard.client.connector.tctoken.TCTokenVerifier;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenHandler implements ConnectorHandler {

    private URI tokenURI;
    private CustomStringBuilder output = new CustomStringBuilder();
    private HashMap<String, String> parameters = new HashMap<String, String>();
    // Charset for HTTP header encoding
    private static final String charset = "UTF-8";

    /**
     * Create a new ActivationRequest.
     */
    public TCTokenHandler() {
    }

    @Override
    public ClientRequest handleRequest(String request) throws Exception {
	try {
	    BufferedReader reader = new BufferedReader(new StringReader(request));
	    String line = reader.readLine();

	    if (line.startsWith("GET")) {
		if (line.contains("eID-Client?tcTokenURL")) {
		    parseURI(line);
		    // Not needed yet.
//		    while (!(line = reader.readLine()).isEmpty()) {
//                        parseParameter(line);
//		    }

		    TCToken token = parseTCToken();

		    TCTokenRequest tcTokenRequest = new TCTokenRequest();
		    tcTokenRequest.setTCToken(token);

		    return tcTokenRequest;
		}
	    }
	    return null;
	} catch (TCTokenException e) {
	    throw e;
	} catch (IllegalArgumentException e) {
	    String message = ConnectorConstants.ConnectorError.BAD_REQUEST.toString();
	    throw new ConnectorException(message, e);
	} catch (IOException e) {
	    String message = ConnectorConstants.ConnectorError.BAD_REQUEST.toString();
	    throw new ConnectorException(message, e);
	} catch (Throwable e) {
	    String message = ConnectorConstants.ConnectorError.INTERNAL_ERROR.toString();
	    throw new ConnectorException(message, e);
	}
    }

    @Override
    public String handleResponse(ClientResponse clientResponse) throws Exception {
	if (clientResponse instanceof TCTokenResponse) {
	    TCTokenResponse response = (TCTokenResponse) clientResponse;

	    if (response.getErrorPage() != null) {
		handleErrorPage(response.getErrorPage());
	    } else if (response.getErrorMessage() != null) {
		handleErrorResponse(response.getErrorMessage());
	    } else if (response.getRefreshAddress() != null) {
		handleRedirectResponse(response.getRefreshAddress());
	    } else {
		handleErrorResponse(ConnectorConstants.ConnectorError.INTERNAL_ERROR.toString());
	    }
	}
	return null;
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
    private TCToken parseTCToken() throws TCTokenException {
	// Get TCToken from the given url
	TCTokenGrabber grabber = new TCTokenGrabber();
	String data = grabber.getResource(tokenURI.toString());

	//FIXME Remove me
	TCTokenConverter converter = new TCTokenConverter();
	data = converter.convert(data);

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new TCTokenException(ConnectorConstants.ConnectorError.TC_TOKEN_NOT_AVAILABLE.toString());
	}

	// Verify the TCToken
	TCTokenVerifier ver = new TCTokenVerifier(tokens);
	ver.verify();

	return tokens.get(0);
    }

    /**
     * Handle a redirect response.
     *
     * @param location Location
     */
    public void handleRedirectResponse(String location) {
	output.appendln("HTTP/1.1 303 See Other");
	output.append("Location: ");
	output.appendln(location);
	output.appendln();
	output.appendln();
    }

    /**
     * Handle a error response.
     *
     * @param message Message
     */
    public void handleErrorResponse(String message) {
	try {
	    ErrorPage p = new ErrorPage(message);
	    String content = p.getHTML();

	    // Header
	    output.appendln("HTTP/1.1 200 OK");
	    output.appendln("Content-Length: " + content.getBytes(charset).length);
	    output.appendln("Connection: close");
	    output.appendln("Content-Type: text/html");
	    // Content
	    output.appendln();
	    output.appendln(content);
	} catch (IOException e) {
	    //TODO
	}
    }

    /**
     * Handle a error HTML page.
     *
     * @param page HTML page
     */
    public void handleErrorPage(String page) {
	try {
	    // Header
	    output.appendln("HTTP/1.1 200 OK");
	    output.appendln("Content-Length: " + page.getBytes(charset).length);
	    output.appendln("Connection: close");
	    output.appendln("Content-Type: text/html");
	    // Content
	    output.appendln();
	    output.appendln(page);
	    output.appendln();
	} catch (IOException e) {
	    //TODO
	}
    }
}
