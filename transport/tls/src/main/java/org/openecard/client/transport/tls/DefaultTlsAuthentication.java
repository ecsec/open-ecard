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
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCredentials;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DefaultTlsAuthentication implements TlsAuthentication {

    private TlsCredentials tlsCredentials;

    public DefaultTlsAuthentication(TlsCredentials tlsCredentials) {
	this.tlsCredentials = tlsCredentials;
    }

    @Override
    public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {
	return this.tlsCredentials;
    }

    @Override
    public void notifyServerCertificate(Certificate certificate) throws IOException {
	/* ignore */
    }

}
