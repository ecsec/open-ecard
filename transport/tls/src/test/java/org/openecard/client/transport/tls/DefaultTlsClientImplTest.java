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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class DefaultTlsClientImplTest {

    @Test(enabled=false)
    public void testopenssl101TLS10() throws IOException {

	URL url = new URL("https://ftei-vm-073.hs-coburg.de:5432/");
	String host = url.getHost();

	DefaultTlsClientImpl tlsClient = new DefaultTlsClientImpl(host, new DefaultTlsAuthentication(null));

	Socket s = new Socket(host, url.getPort());
	TlsProtocolHandler handler = new TlsProtocolHandler(s.getInputStream(), s.getOutputStream());
	handler.connect(tlsClient);
	handler.close();

    }

    @Test(enabled=false)
    public void testopenssl101TLS11() throws IOException {

	URL url = new URL("https://ftei-vm-073.hs-coburg.de:5432/");
	String host = url.getHost();

	DefaultTlsClientImpl tlsClient = new DefaultTlsClientImpl(host, new DefaultTlsAuthentication(null));
	//TODO reenable when bouncycaslte artifact is updated
	//tlsClient.setClientVersion(ProtocolVersion.TLSv11);
	Socket s = new Socket(host, url.getPort());
	TlsProtocolHandler handler = new TlsProtocolHandler(s.getInputStream(), s.getOutputStream());
	handler.connect(tlsClient);
	handler.close();

    }

    @Test(enabled=false)
    public void testfacebookTLS10() throws IOException {

	URL url = new URL("https://www.facebook.de:443/");
	String host = url.getHost();

	DefaultTlsClientImpl tlsClient = new DefaultTlsClientImpl(host, new DefaultTlsAuthentication(null));

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
	Assert.assertTrue(sb.toString().contains("html"));

    }

    @Test(enabled=false)
    public void testfacebookTLS11() throws IOException {

	URL url = new URL("https://www.facebook.de:443/");
	String host = url.getHost();

	DefaultTlsClientImpl tlsClient = new DefaultTlsClientImpl(host, new DefaultTlsAuthentication(null));
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
	Assert.assertTrue(sb.toString().contains("html"));

    }

    @Test(enabled=false)
    public void testgoogleTLS10() throws IOException {

	URL url = new URL("https://www.google.de:443/");
	String host = url.getHost();

	DefaultTlsClientImpl tlsClient = new DefaultTlsClientImpl(host, new DefaultTlsAuthentication(null));

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
	Assert.assertTrue(sb.toString().contains("html"));

    }

    @Test(enabled=false)
    public void testgoogleTLS11() throws IOException {

	URL url = new URL("https://www.google.de:443/");
	String host = url.getHost();

	DefaultTlsClientImpl tlsClient = new DefaultTlsClientImpl(host, new DefaultTlsAuthentication(null));
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
	Assert.assertTrue(sb.toString().contains("html"));

    }
    
}
