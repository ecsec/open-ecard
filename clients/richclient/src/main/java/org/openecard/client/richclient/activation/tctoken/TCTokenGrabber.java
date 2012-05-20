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

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.richclient.activation.common.ActivationConstants;
import org.openecard.client.richclient.activation.io.LimitedInputStream;

/**
 * Implements a grabber to fetch TCTokens from a URI.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenGrabber {

    /**
     * Opens a stream to the given URI.
     *
     * @param uri URI
     * @return Resource as a stream
     * @throws TCTokenException
     */
    public InputStream getStream(String uri) throws TCTokenException {
	try {
	    // Decode "application/x-www-form-urlencoded" string to UTF-8
	    String decodedURL = java.net.URLDecoder.decode(uri, "UTF-8");
	    URL url = new URL(decodedURL);

	    // Open connection and stream
	    URLConnection con = url.openConnection();
	    LimitedInputStream is = new LimitedInputStream(con.getInputStream());

	    return is;
	} catch (Exception e) {
	    String message = ActivationConstants.ActivationError.TC_TOKEN_NOT_AVAILABLE.toString();
	    Logger.getLogger(TCTokenGrabber.class.getName()).log(Level.SEVERE, message, e);
	    throw new TCTokenException(message, e);
	}

    }

    /**
     * Fetch the data from the URI.
     *
     * @param uri URI
     * @return Resource fetched from the URI
     * @throws TCTokenException
     */
    public String getResource(String uri) throws TCTokenException {
	LimitedInputStream is = null;

	try {
	    is = new LimitedInputStream(getStream(uri));
	    StringBuilder sb = new StringBuilder();
	    byte[] buf = new byte[1024];

	    while (true) {
		int num = is.read(buf);
		if (num == -1) {
		    break;
		}
		sb.append(new String(buf, 0, num));
	    }

	    return sb.toString();
	} catch (Exception e) {
	    String message = ActivationConstants.ActivationError.TC_TOKEN_NOT_AVAILABLE.toString();
	    Logger.getLogger(TCTokenGrabber.class.getName()).log(Level.SEVERE, message, e);
	    throw new TCTokenException(message, e);
	} finally {
	    try {
		is.close();
	    } catch (Exception ignore) {
	    }
	}
    }
}
