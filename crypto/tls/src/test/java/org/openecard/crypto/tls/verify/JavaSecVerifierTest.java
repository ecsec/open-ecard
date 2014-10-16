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
import java.security.SecureRandom;
import org.openecard.bouncycastle.crypto.tls.TlsClientProtocol;
import org.testng.SkipException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
@Test(groups={"it"})
public class JavaSecVerifierTest {

    @Test
    public void testVerificationNoError() throws IOException {
	final String hostName = "www.heise.de";
	TlsClientProtocol handler;
	DefaultTlsClientImpl c;
	try {
	    // open connection
	    Socket socket = new Socket(hostName, 443);
	    assertTrue(socket.isConnected());
	    // connect client
	    c = new DefaultTlsClientImpl(hostName);
	    handler = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream(), new SecureRandom());
	} catch (Exception ex) {
	    throw new SkipException("Unable to create TLS client.");
	}
	// do TLS handshake
	handler.connect(c);
	handler.close();
    }

    @Test(expectedExceptions=IOException.class)
    public void testVerificationError() throws IOException {
	final String hostName = "www.google.com";
	final String actualHostName = "www.heise.de";
	TlsClientProtocol handler;
	DefaultTlsClientImpl c;
	try {
	    // open connection
	    Socket socket = new Socket(actualHostName, 443);
	    assertTrue(socket.isConnected());
	    // connect client
	    c = new DefaultTlsClientImpl(hostName);
	    handler = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream(), new SecureRandom());
	} catch (Exception ex) {
	    throw new SkipException("Unable to create TLS client.");
	}
	// do TLS handshake
	handler.connect(c);
	handler.close();
    }

}
