/****************************************************************************
 * Copyright (C) 2013-2017 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.openecard.bouncycastle.tls.TlsClientProtocol;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.FileUtils;
import org.openecard.httpcore.HttpRequestHelper;
import org.openecard.httpcore.HttpUtils;
import org.openecard.httpcore.StreamHttpClientConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;


/**
 *
 * @author Tobias Wich
 */
public class HttpGetTask implements Callable<StartPAOSResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpGetTask.class);

    private final Dispatcher dispatcher;
    private final ConnectionHandleType connectionHandle;
    private final TCTokenRequest tokenRequest;

    public HttpGetTask(Dispatcher dispatcher, ConnectionHandleType connectionHandle, TCTokenRequest tokenRequest) {
	this.dispatcher = dispatcher;
	this.connectionHandle = connectionHandle;
	this.tokenRequest = tokenRequest;
    }

    @Override
    public StartPAOSResponse call() throws Exception {
	try {
	    getRequest();
	} finally {
	    TCTokenHandler.disconnectHandle(dispatcher, connectionHandle);
	}

	// produce a positive result
	StartPAOSResponse response = WSHelper.makeResponse(StartPAOSResponse.class, WSHelper.makeResultOK());
	return response;
    }

    private void getRequest() throws IOException, ConnectionError, URISyntaxException, HttpException {
	TlsConnectionHandler tlsHandler = new TlsConnectionHandler(dispatcher, tokenRequest, connectionHandle);
	tlsHandler.setUpClient();

	// connect the tls endpoint and make a get request
	TlsClientProtocol handler = tlsHandler.createTlsConnection();

	// set up connection to endpoint
	InputStream in = handler.getInputStream();
	OutputStream out = handler.getOutputStream();
	StreamHttpClientConnection conn = new StreamHttpClientConnection(in, out);

	// prepare HTTP connection
	HttpContext ctx = new BasicHttpContext();
	HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
	DefaultConnectionReuseStrategy reuse = new DefaultConnectionReuseStrategy();

	// prepare request
	String resource = tlsHandler.getResource();
	BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("GET", resource);
	HttpRequestHelper.setDefaultHeader(req, tlsHandler.getServerAddress());
	req.setHeader("Accept", "text/html, */*;q=0.8");
	req.setHeader("Accept-Charset", "utf-8, *;q=0.8");
	HttpUtils.dumpHttpRequest(LOG, req);

	// send request and receive response
	HttpResponse response = httpexecutor.execute(req, conn, ctx);
	int statusCode = response.getStatusLine().getStatusCode();
	conn.receiveResponseEntity(response);
	HttpEntity entity = response.getEntity();
	byte[] entityData = FileUtils.toByteArray(entity.getContent());
	HttpUtils.dumpHttpResponse(LOG, response, entityData);
	conn.close();

	if (statusCode < 200 || statusCode > 299) {
	    throw new ConnectionError(WRONG_SERVER_RESULT, statusCode);
	}
    }

}
