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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;
import org.openecard.bouncycastle.crypto.tls.TlsClient;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TlsClientSocketFactory extends SSLSocketFactory {

    TlsClient client;

    public TlsClientSocketFactory(TlsClient client) {
	super();
	this.client = client;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
	return new TlsClientSocket(address.getHostName(), port, client);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
	return new TlsClientSocket(host, port, client);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
	return new TlsClientSocket(host.getHostName(), port, client);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
	return new TlsClientSocket(host, port, client);
    }

    @Override
    public String[] getSupportedCipherSuites() {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public String[] getDefaultCipherSuites() {
	throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
	return new TlsClientSocket(host, port, client);
    }

}
