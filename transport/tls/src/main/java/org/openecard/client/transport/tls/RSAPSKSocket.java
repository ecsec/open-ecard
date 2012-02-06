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
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.openecard.bouncycastle.crypto.tls.TlsProtocolHandler;

/**
 * {@link Socket}-Extension, needed for replacement of in- and outputstream with
 * the ones from {@link TlsProtocolHandler}
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class RSAPSKSocket extends Socket {

    private InputStream is = null;
    private OutputStream os = null;

    public RSAPSKSocket() {
	super();
    }

    public RSAPSKSocket(String host, int port) throws UnknownHostException, IOException {
	super(host, port);
    }

    public void setInputStream(InputStream is) {
	this.is = is;
    }

    public void setOutputStream(OutputStream os) {
	this.os = os;
    }

    public InputStream getInputStream() throws IOException {
	return (is == null) ? super.getInputStream() : is;
    }

    public OutputStream getOutputStream() throws IOException {
	return (os == null) ? super.getOutputStream() : os;
    }
}
