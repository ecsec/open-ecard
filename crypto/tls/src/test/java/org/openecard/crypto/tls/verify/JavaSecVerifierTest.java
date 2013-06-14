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

package org.openecard.crypto.tls.verify;

import java.io.IOException;
import java.net.Socket;
import org.openecard.bouncycastle.crypto.tls.TlsClientProtocol;
import org.testng.SkipException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class JavaSecVerifierTest {

    @Test
    public void testVerificationNoError() throws IOException {
	final String hostName = "www.google.com";
	TlsClientProtocol handler;
	try {
	    // open connection
	    Socket socket = new Socket(hostName, 443);
	    assertTrue(socket.isConnected());
	    // connect client
	    DefaultTlsClientImpl c = new DefaultTlsClientImpl(hostName);
	    handler = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream());
	    handler.connect(c);
	} catch (Exception ex) {
	    throw new SkipException("Unable to create TLS client.");
	}
    }

    // TODO: enable as soon as the modified bouncycastle does not block in case of an error anymore
    //@Test(expectedExceptions=IOException.class)
    public void testVerificationError() throws IOException {
	final String hostName = "www.google.com";
	final String actualHostName = "www.verisign.de";
	TlsClientProtocol handler = null;
	DefaultTlsClientImpl c = null;
	try {
	    // open connection
	    Socket socket = new Socket(actualHostName, 443);
	    assertTrue(socket.isConnected());
	    // connect client
	    c = new DefaultTlsClientImpl(hostName);
	    handler = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream());
	} catch (Exception ex) {
	    throw new SkipException("Unable to create TLS client.");
	}
	// do TLS handshake
	handler.connect(c);
    }

}
