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
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
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
    public void testPSKwithTLS10() throws IOException {
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
	System.out.println(sb.toString());
	Assert.assertTrue(sb.toString().contains("PSK"));
    }

    @Test(enabled=false)
    public void testPSKwithTLS11() throws IOException {
	URL url = new URL("https://ftei-vm-073.hs-coburg.de:8888/");
	String host = url.getHost();
	byte[] identity = new String("pskuser").getBytes();
	byte[] psk = Hex.decode("0e8b59a3bc6cf8097b0aeb453d558266");

	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(new TlsPSKIdentityImpl(identity, psk), host);
	//TODO reenable when bouncycaslte artifact is updated
	//tlsClient.setClientVersion(ProtocolVersion.TLSv11);
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
	System.out.println(sb.toString());
	Assert.assertTrue(sb.toString().contains("PSK"));
    }


    /**
     * Test TLS-RSA-PSK
     *
     * @throws IOException
     */
    @Test(enabled=false)
    public void testRSAPSKopenssl() throws IOException {

	URL url = new URL("https://ftei-vm-073.hs-coburg.de:4433");
	String host = url.getHost();
	byte[] identity = "Client_identity".getBytes();
	byte[] psk = Hex.decode("1234");

	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(new TlsPSKIdentityImpl(identity, psk), host);
	tlsClient.removeClientExtension(0);

	Socket s = new Socket(host, url.getPort());
	TlsProtocolHandler handler = new TlsProtocolHandler(s.getInputStream(), s.getOutputStream());
	handler.connect(tlsClient);
	handler.close();

    }

    @Test(enabled=false) // needs fresh sessionid + psk
    public void testRSAPSKageto() throws IOException {

	URL url = new URL("https://eid-ref.eid-service.de:443");
	String host = url.getHost();
	byte[] identity = "330f77885369105f76d6d1d09332".getBytes();
	byte[] psk = Hex.decode("4a2af578cf056901df079aec3d457e2883c1d00eb4d3493a678ebd18dc7a8ebf");

	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(new TlsPSKIdentityImpl(identity, psk), host);
	tlsClient.removeClientExtension(0);

	Socket s = new Socket(host, url.getPort());
	TlsProtocolHandler handler = new TlsProtocolHandler(s.getInputStream(), s.getOutputStream());
	handler.connect(tlsClient);
	handler.close();

    }

    @Test(enabled=false) // needs fresh sessionid + psk
    public void testRSAPSmtg() throws IOException {

	URL url = new URL("https://fry.mtg.de:443");
	String host = url.getHost();
	byte[] identity = "C08A8834D4B3989E8630F761BAAD267AC6F692A74D5B45F5AD45D5E21D3B82AA".getBytes();
	byte[] psk = Hex.decode("4BEDFF4FED91A9F5BAE234CDC0840C318110457FD28BB41D60EC59C087351ECD");

	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(new TlsPSKIdentityImpl(identity, psk), host);
	tlsClient.removeClientExtension(0);

	Socket s = new Socket(host, url.getPort());
	TlsProtocolHandler handler = new TlsProtocolHandler(s.getInputStream(), s.getOutputStream());
	handler.connect(tlsClient);
	handler.close();

    }

    @Test(enabled=false) // needs fresh sessionid + psk
    public void testRSAPSKgovernikus() throws IOException {

	URL url = new URL("https://testpaos.governikus-eid.de:443/ecardpaos/paosreceiver");
	String host = url.getHost();
	byte[] identity = "37a2b5e667ac09bf91f3c4271fdae80fc690d3f2".getBytes();
	byte[] psk = Hex
		.decode("e45cb2a16da683ffad01824ee9c0b6d0ea3bfa8bdb49fdb17fe746f631e251a573c9a528a3dd9e25e09ff3c9e5cf6eb09bec4cd9fc1bf0cf63e52d9319f05d27");

	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(new TlsPSKIdentityImpl(identity, psk), host);
	tlsClient.removeClientExtension(0);

	TlsClientSocketFactory tlsClientSocketFactory = new TlsClientSocketFactory(tlsClient);

	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	conn.setSSLSocketFactory(tlsClientSocketFactory);
	conn.setDoOutput(true);
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
	    conn.disconnect();
	}
	System.out.println(sb.toString());
    }
}
