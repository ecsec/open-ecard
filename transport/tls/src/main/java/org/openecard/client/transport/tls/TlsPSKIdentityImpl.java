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

import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentity;

/**
 * Simple Implementation for {@link TlsPSKIdentity}
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TlsPSKIdentityImpl implements TlsPSKIdentity {

    private final byte[] identity;
    private final byte[] psk;

    public TlsPSKIdentityImpl(byte[] identity, byte[] psk) {
	this.identity = identity;
	this.psk = psk;
    }

    @Override
    public byte[] getPSK() {
	return psk;
    }

    @Override
    public byte[] getPSKIdentity() {
	return identity;
    }

    @Override
    public void notifyIdentityHint(byte[] arg0) {
	// System.out.println("Received IdentityHint: " + new String(arg0));
    }

    @Override
    public void skipIdentityHint() {
	// OK
    }

}
