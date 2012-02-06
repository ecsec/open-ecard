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
import org.openecard.bouncycastle.crypto.tls.PSKTlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCipherFactory;
import org.openecard.bouncycastle.crypto.tls.TlsCredentials;
import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentity;

/**
 * Extension of {@link PSKTlsClient} to override getAuthentication-Method to
 * return something else than NULL
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class PSKTlsClientImpl extends PSKTlsClient {

    public PSKTlsClientImpl(TlsCipherFactory cipherFactory, TlsPSKIdentity pskIdentity) {
	super(cipherFactory, pskIdentity);
    }

    public PSKTlsClientImpl(byte[] identity, byte[] psk){
	super(new TlsPSKIdentityImpl(identity, psk));
    }
    
    public PSKTlsClientImpl(TlsPSKIdentity pskIdentity) {
	super(pskIdentity);
    }

    @Override
    public TlsAuthentication getAuthentication() throws IOException {
	return new TlsAuthentication() {

	    @Override
	    public void notifyServerCertificate(Certificate arg0) throws IOException {
		// TODO verify certificates
		// X509CertificateStructure[] x = arg0.getCerts();
		// for (X509CertificateStructure x509CertificateStructure : x) {
		// System.out.println(x509CertificateStructure.getIssuer());
		// }
	    }

	    @Override
	    public TlsCredentials getClientCredentials(CertificateRequest arg0) throws IOException {
		// not needed for RSA-PSK
		return null;
	    }
	};
    }

}
