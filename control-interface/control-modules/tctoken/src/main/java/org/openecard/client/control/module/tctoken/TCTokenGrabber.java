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

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.openecard.client.common.io.LimitedInputStream;


/**
 * Implements a grabber to fetch TCTokens from a URI.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
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
	    throw new TCTokenException("TCToken not available", e);
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
	    throw new TCTokenException("TCToken not available", e);
	} finally {
	    try {
		is.close();
	    } catch (Exception ignore) {
	    }
	}
    }

}
