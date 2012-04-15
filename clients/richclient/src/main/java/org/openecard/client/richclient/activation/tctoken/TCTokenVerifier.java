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
package org.openecard.client.richclient.activation.tctoken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openecard.client.richclient.activation.common.ActivationConstants.ActivationError;
import org.openecard.client.common.util.ByteUtils;


/**
 * Implements a verifier to check the elements of a TCToken.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenVerifier {

    private List<TCToken> tokens;
    private TCToken token;

    /**
     * Creates a new TCTokenVerifier to verify a TCToken.
     *
     * @param tokens Tokens
     */
    public TCTokenVerifier(List<TCToken> tokens) {
	this.tokens = tokens;
    }

    /**
     * Creates a new TCTokenVerifier to verify a TCToken.
     *
     * @param token Token
     */
    public TCTokenVerifier(TCToken token) {
	tokens = new ArrayList<TCToken>();
	tokens.add(token);
    }

    /**
     * Verifies the elements of the TCToken.
     *
     * @throws TCTokenException
     */
    public void verify() throws TCTokenException {
	if (tokens.isEmpty()) {
	    String message = ActivationError.TC_TOKEN_NOT_AVAILABLE.toString();
	    Logger.getLogger(TCTokenVerifier.class.getName()).log(Level.SEVERE, message);
	    throw new TCTokenException(message);
	}

	try {
	    for (Iterator<TCToken> it = tokens.iterator(); it.hasNext();) {
		token = it.next();

		verifyServerAddress();
		verifySessionIdentifier();
		verifyRefreshAddress();
		verifyBinding();
		verifyPathSecurityParameter();
		verifyPathSecurityProtocol();
	    }
	} catch (Exception e) {
	    String message = ActivationError.TC_TOKEN_REFUSED.toString();
	    Logger.getLogger(TCTokenVerifier.class.getName()).log(Level.SEVERE, message, e);
	    throw new TCTokenException(message, e);
	}
    }

    /**
     * Verifies the ServerAddress element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyServerAddress() throws Exception {
	String value = token.getServerAddress();
	checkEmpty(value);
	//FIXME URI von den providern ist malformed!
//        isHTTTPS(value);
    }

    /**
     * Verifies the SessionIdentifier element of the TCToken.
     *
     * @throws Exception
     */
    public void verifySessionIdentifier() throws Exception {
	String value = token.getSessionIdentifier();
	checkRequired(value);
	//FIXME 16
	checkHexLength(value, 8);
	//TODO check length of the sessionID
    }

    /**
     * Verifies the RefreshAddress element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyRefreshAddress() throws Exception {
	String value = token.getRefreshAddress();
	checkRequired(value);
    }

    /**
     * Verifies the Binding element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyBinding() throws Exception {
	String value = token.getBinding();
	checkRequired(value);
	checkEqual(value, "urn:liberty:paos:2006-08");
    }

    /**
     * Verifies the PathSecurity-Protocol element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyPathSecurityProtocol() throws Exception {
	String value = token.getPathSecurityProtocol();
	if (!checkEmpty(value)) {
	    checkEqual(value, "urn:ietf:rfc:4279");
	    checkEqual(value, "urn:ietf:rfc:5487");
	}
    }

    /**
     * Verifies the PathSecurity-Parameter element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyPathSecurityParameter() throws Exception {
	TCToken.PathSecurityParameter psp = token.getPathSecurityParameter();
	if (!checkEmpty(psp)) {
	    checkRequired(psp.getPSK());
	    checkHexLength(ByteUtils.toHexString(psp.getPSK()), 16);
	}
    }

    /**
     * Checks if the value is "empty".
     *
     * @param value Value
     * @return True if the element is empty, otherwise false
     * @throws Exception
     */
    private boolean checkEmpty(Object value) throws Exception {
	if (value != null) {
	    if (value instanceof String) {
		if (!((String) value).isEmpty()) {
		    return true;
		}
	    } else if (value instanceof byte[]) {
		if (((byte[]) value).length != 0) {
		    return true;
		}
	    }
	    return false;
	}
	return false;
    }

    /**
     * Checks if the value is equal.
     *
     * @param value Value
     * @param equal Equal
     * @throws Exception
     */
    private void checkEqual(String value, String equal) throws Exception {
	if (!value.equals(equal)) {
	    throw new Exception("Element is not equal to " + equal);
	}
    }

    /**
     * Checks if the element is present.
     *
     * @param value Value
     * @throws Exception
     */
    private void checkRequired(Object value) throws Exception {
	if (!checkEmpty(value)) {
	    throw new Exception("Element is required.");
	}
    }

    /**
     * Checks if the value contains enough hex bytes.
     *
     * @param value Value
     * @param minOccurrence Min occurrence
     * @throws Exception
     */
    private void checkHexLength(String value, int minOccurrence) throws Exception {
	Pattern p = Pattern.compile("\\p{XDigit}{2}");
	Matcher m = p.matcher(value);

	int count = 0;
	while (m.find()) {
	    count++;
	}
	if (count < minOccurrence) {
	    throw new Exception("The length of the value is " + count + " bytes, expected are " + minOccurrence);
	}
    }

}
