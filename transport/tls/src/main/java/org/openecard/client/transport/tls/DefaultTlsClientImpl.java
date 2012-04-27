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
import org.openecard.bouncycastle.crypto.tls.DefaultTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;

/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DefaultTlsClientImpl extends DefaultTlsClient {

    private TlsAuthentication tlsAuthentication;

    /**
     * 
     * @param host Hostname as Fully Qualified Domain Name (FQDN)
     * @param tlsAuthentication Authentication to use for this client
     */
    public DefaultTlsClientImpl(String host, TlsAuthentication tlsAuthentication) {
	super(host);
	this.tlsAuthentication = tlsAuthentication;
    }

    @Override
    public TlsAuthentication getAuthentication() throws IOException {
	return tlsAuthentication;
    }

}
