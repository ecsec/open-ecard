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

package org.openecard.transport.httpcore;

import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import org.openecard.apache.http.Header;
import org.openecard.apache.http.HttpEntity;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.impl.DefaultConnectionReuseStrategy;
import org.openecard.apache.http.message.BasicHttpRequest;
import org.openecard.apache.http.protocol.BasicHttpContext;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.apache.http.protocol.HttpRequestExecutor;
import org.openecard.apache.http.util.EntityUtils;
import org.openecard.bouncycastle.crypto.tls.DefaultTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsClientProtocol;
import org.openecard.common.util.FileUtils;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;


/**
 *
 * @author Tobias Wich
 */
@Test(groups = "it")
public class StreamHttpClientConnectionTest {

    private SecureRandom rand;

    @BeforeClass
    public void setup() {
	rand = new SecureRandom();
    }

    @Test
    public void testRequestHttpGoogle() throws IOException, HttpException {
	final String hostName = "www.google.com";
	// open connection
	Socket socket = new Socket(hostName, 80);
	assertTrue(socket.isConnected());
	StreamHttpClientConnection conn = new StreamHttpClientConnection(socket.getInputStream(), socket.getOutputStream());
	assertTrue(conn.isOpen());

	consumeEntity(conn, hostName, 2);
    }

    @Test
    public void testRequestHttpsGoogle() throws IOException, HttpException {
	final String hostName = "www.google.com";
	// open connection
	Socket socket = new Socket(hostName, 443);
	assertTrue(socket.isConnected());
	DefaultTlsClient tlsClient = new DefaultTlsClientImpl();
	tlsClient.setServerName(hostName);
	TlsClientProtocol handler = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream(), rand);
	handler.connect(tlsClient);
	StreamHttpClientConnection conn = new StreamHttpClientConnection(handler.getInputStream(), handler.getOutputStream());
	assertTrue(conn.isOpen());

	consumeEntity(conn, hostName, 2);
    }

    private void consumeEntity(StreamHttpClientConnection conn, String hostName, int numIt) throws IOException, HttpException {
	HttpContext ctx = new BasicHttpContext();
	HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
	HttpResponse response = null;
	DefaultConnectionReuseStrategy reuse = new DefaultConnectionReuseStrategy();

	int i = 0;
	while (i == 0 || (i < numIt && reuse.keepAlive(response, ctx))) {
	    i++;
	    // send request and receive response
	    HttpRequest request = new BasicHttpRequest("GET", "/");
	    request.setParams(conn.getParams());
	    HttpRequestHelper.setDefaultHeader(request, hostName);
	    response = httpexecutor.execute(request, conn, ctx);
	    conn.receiveResponseEntity(response);
	    HttpEntity entity = response.getEntity();
	    assertNotNull(entity);

	    // consume entity
	    byte[] content = FileUtils.toByteArray(entity.getContent());

	    // read header and check if content size is correct
	    Header lengthHeader = response.getFirstHeader("Content-Length");
	    long length = Long.parseLong(lengthHeader.getValue());
	    assertNotNull(lengthHeader);
	    assertEquals(entity.getContentLength(), length);
	    assertEquals(content.length, length);

	    // consume everything from the entity and close stream
	    EntityUtils.consume(entity);
	}
    }

}
