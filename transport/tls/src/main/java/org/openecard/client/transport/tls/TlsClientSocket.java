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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private TlsProtocolHandler tlsProtocolHandler;
    private boolean wantClientAuth = false;
    
    public TlsClientSocket(String host, int port, TlsClient client) throws UnknownHostException, IOException {
        this.connect(new InetSocketAddress(host, port));
        this.client = client;
        tlsProtocolHandler = new TlsProtocolHandler(super.getInputStream(), super.getOutputStream());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
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
        return new TlsClientSSLSession();
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
       return this.wantClientAuth;
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
        // TODO
        return;
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
        this.wantClientAuth = want;
    }

    @Override
    public void startHandshake() throws IOException {
        tlsProtocolHandler.connect(this.client);
    }

}
