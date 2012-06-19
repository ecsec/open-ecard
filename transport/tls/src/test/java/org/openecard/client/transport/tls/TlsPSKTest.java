/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.transport.tls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.openecard.bouncycastle.crypto.tls.TlsProtocolHandler;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TlsPSKTest {

    /**
     * Test TLS-PSK without RSA
     *
     * @throws IOException
     */
    @Test(enabled=false) //works only from inside the HS
    public void testPSK() throws IOException {
	URL url = new URL("https://ftei-vm-073.hs-coburg.de:8888/");
	String host = url.getHost();
	byte[] identity = new String("pskuser").getBytes();
	byte[] psk = Hex.decode("0e8b59a3bc6cf8097b0aeb453d558266");

	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(new TlsPSKIdentityImpl(identity, psk), host);

	TlsClientSocketFactory tlsClientSocketFactory = new TlsClientSocketFactory(tlsClient);

	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	conn.setSSLSocketFactory(tlsClientSocketFactory);
	conn.connect();

	InputStream response = null;
	StringBuilder sb = new StringBuilder();
	try {
	    response = conn.getInputStream();
	    InputStreamReader isr = new InputStreamReader(response);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    while ((line = br.readLine()) != null) {
		sb.append(line);
		sb.append("\n");
	    }
	} finally {
	    response.close();
	}
	// Server will response with some infos, including chosen Ciphersuite
	Assert.assertTrue(sb.toString().contains("PSK"));
    }

    /**
     * Test TLS-RSA-PSK
     *
     * @throws IOException
     */
    @Test(enabled=false)
    public void testRSAPSK() throws IOException {

	URL url = new URL("https://ftei-vm-073.hs-coburg.de:4433/");
	String host = url.getHost();
	byte[] identity = "Client_identity".getBytes();
	byte[] psk = Hex.decode("1234");

	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(new TlsPSKIdentityImpl(identity, psk), host);

	// TODO exchange the following 4 lines with socket stuff below
	Socket s = new Socket(host, url.getPort());
	TlsProtocolHandler handler = new TlsProtocolHandler(s.getInputStream(), s.getOutputStream());
	handler.connect(tlsClient);
	handler.close();

	/*
	 * TlsClientSocketFactory tlsClientSocketFactory = new
	 * TlsClientSocketFactory(tlsClient);
	 *
	 * HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	 * conn.setSSLSocketFactory(tlsClientSocketFactory); conn.connect();
	 *
	 * InputStream response = null; StringBuilder sb = new StringBuilder();
	 * try { response = conn.getInputStream(); InputStreamReader isr = new
	 * InputStreamReader(response); BufferedReader br = new
	 * BufferedReader(isr); String line; while((line = br.readLine()) !=
	 * null){ sb.append(line); sb.append("\n"); } } finally {
	 * response.close(); } // Server will response with some infos,
	 * including chosen Ciphersuite
	 * Assert.assertTrue(sb.toString().contains("PSK"));
	 */
    }

}
