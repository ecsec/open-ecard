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
import org.openecard.bouncycastle.crypto.tls.SRPTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;


/**
 * A TLS-Client that implements the SRP-Protocol.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 */
public class SRPTlsClientImpl extends SRPTlsClient {

    public SRPTlsClientImpl(byte[] identity, byte[] password, String host) {
	super(identity, password, host);
    }

    @Override
    public TlsAuthentication getAuthentication() throws IOException {
	return new DefaultTlsAuthentication(null);
    }

}
