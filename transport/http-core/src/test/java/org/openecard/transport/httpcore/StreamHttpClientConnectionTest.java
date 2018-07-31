/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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
import java.util.Collections;
import java.util.Vector;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;
import org.openecard.bouncycastle.tls.DefaultTlsClient;
import org.openecard.bouncycastle.tls.TlsClientProtocol;
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;
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
	DefaultTlsClient tlsClient = new DefaultTlsClientImpl(new BcTlsCrypto(rand)) {
	    @Override
	    protected Vector getSNIServerNames() {
		return new Vector(Collections.singletonList(hostName));
	    }
	};
	TlsClientProtocol handler = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream());
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
