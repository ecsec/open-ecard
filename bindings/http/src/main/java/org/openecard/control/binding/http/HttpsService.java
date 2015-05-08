/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.control.binding.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.List;
import org.openecard.apache.http.HttpRequestInterceptor;
import org.openecard.apache.http.HttpResponseInterceptor;
import org.openecard.apache.http.protocol.HttpRequestHandler;
import org.openecard.bouncycastle.crypto.tls.TlsServerProtocol;
import org.openecard.crypto.tls.SocketWrapper;
import org.openecard.common.util.SecureRandomFactory;


/**
 *
 * @author Tobias Wich
 */
public class HttpsService extends HttpService {

    public HttpsService(int port, HttpRequestHandler handler, List<HttpRequestInterceptor> reqInterceptors,
	    List<HttpResponseInterceptor> respInterceptors) throws Exception {
	super(port, handler, reqInterceptors, respInterceptors);
    }

    @Override
    protected Socket accept() throws IOException, HttpServiceError, HttpsServiceError {
	Socket plainSocket = super.accept();

	LocalKeystoreTlsServer tlsServer = new LocalKeystoreTlsServer();
	InputStream plainIn = plainSocket.getInputStream();
	OutputStream plainOut = plainSocket.getOutputStream();
	SecureRandom rand = SecureRandomFactory.create(32);
	TlsServerProtocol handler = new TlsServerProtocol(plainIn, plainOut, rand);
	handler.accept(tlsServer);

	Socket secSocket = new SocketWrapper(plainSocket, handler.getInputStream(), handler.getOutputStream());
	return secSocket;
    }

}
