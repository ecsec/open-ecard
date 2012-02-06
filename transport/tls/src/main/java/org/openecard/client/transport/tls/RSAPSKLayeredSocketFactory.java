/*
 * Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.transport.tls;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.params.HttpParams;
import org.openecard.bouncycastle.crypto.tls.TlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsProtocolHandler;

/**
 * {@link LayeredSocketFactory}-Implementation for TLS-RSA-PSK
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
@SuppressWarnings("deprecation")
// new api not yet in android
public class RSAPSKLayeredSocketFactory implements LayeredSocketFactory {

    private TlsClient client;

    public RSAPSKLayeredSocketFactory(TlsClient client) {
	this.client = client;
    }

    @Override
    public boolean isSecure(Socket arg0) throws IllegalArgumentException {
	return true;
    }

    @Override
    public Socket createSocket() throws IOException {
	//unused
	return null;
    }

    @Override
    public Socket connectSocket(Socket arg0, String arg1, int arg2, InetAddress arg3, int arg4, HttpParams arg5) throws IOException,
	    UnknownHostException, ConnectTimeoutException {
	// unused
	return null;
    }

    @Override
    public Socket createSocket(Socket sock, String host, int port, boolean arg3) throws IOException, UnknownHostException {
	sock = new RSAPSKSocket(host, port);
	TlsProtocolHandler tlsProtocolHandler = new TlsProtocolHandler(sock.getInputStream(), sock.getOutputStream());
	tlsProtocolHandler.connect(this.client);
	((RSAPSKSocket) sock).setInputStream(tlsProtocolHandler.getInputStream());
	((RSAPSKSocket) sock).setOutputStream(tlsProtocolHandler.getOutputStream());
	return sock;
    }

}
