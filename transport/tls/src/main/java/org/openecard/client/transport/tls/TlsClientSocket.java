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
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.openecard.bouncycastle.crypto.tls.TlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsProtocolHandler;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TlsClientSocket extends SSLSocket {

    private TlsClient client;
    TlsProtocolHandler tlsProtocolHandler;

    public TlsClientSocket(String host, int port, TlsClient client) throws UnknownHostException, IOException {
	this.connect(new InetSocketAddress(host, port));
	this.client = client;
	tlsProtocolHandler = new TlsProtocolHandler(super.getInputStream(), super.getOutputStream());
    }

    @Override
    public java.io.OutputStream getOutputStream() throws IOException {
	return (tlsProtocolHandler != null) ? tlsProtocolHandler.getOutputStream() : super.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
	return (tlsProtocolHandler != null) ? tlsProtocolHandler.getInputStream() : super.getInputStream();
    }

    @Override
    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
	// TODO
    }

    @Override
    public boolean getEnableSessionCreation() {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public String[] getEnabledCipherSuites() {
	return null;
    }

    @Override
    public String[] getEnabledProtocols() {
	// TODO
	return null;
    }

    @Override
    public boolean getNeedClientAuth() {
	// TODO
	return false;
    }

    @Override
    public SSLSession getSession() {
	// TODO
	return null;
    }

    @Override
    public String[] getSupportedCipherSuites() {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public String[] getSupportedProtocols() {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean getUseClientMode() {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean getWantClientAuth() {
	// TODO
	return false;
    }

    @Override
    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
	throw new RuntimeException("Not yet implemented");

    }

    @Override
    public void setEnableSessionCreation(boolean flag) {
	throw new RuntimeException("Not yet implemented");

    }

    @Override
    public void setEnabledCipherSuites(String[] suites) {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void setEnabledProtocols(String[] protocols) {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void setNeedClientAuth(boolean need) {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void setUseClientMode(boolean mode) {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void setWantClientAuth(boolean want) {
	// TODO
    }

    @Override
    public void startHandshake() throws IOException {
	tlsProtocolHandler.connect(this.client);
    }

}
