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
import javax.net.ssl.SSLSocketFactory;
import org.openecard.bouncycastle.crypto.tls.TlsClient;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class TLSClientSocketFactory extends SSLSocketFactory {

    TlsClient client;

    public TLSClientSocketFactory(TlsClient client) {
	super();
	this.client = client;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
	return new TLSPSKSocket(address.getHostName(), port, client);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
	return new TLSPSKSocket(host, port, client);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
	return new TLSPSKSocket(host.getHostName(), port, client);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
	return new TLSPSKSocket(host, port, client);
    }

    @Override
    public String[] getSupportedCipherSuites() {
	// TODO
	return null;
    }

    @Override
    public String[] getDefaultCipherSuites() {
	// TODO
	return null;

    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
	return new TLSPSKSocket(host, port, client);
    }

}
