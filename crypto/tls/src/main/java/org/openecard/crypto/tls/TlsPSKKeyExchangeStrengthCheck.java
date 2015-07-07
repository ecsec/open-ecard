/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import org.openecard.bouncycastle.crypto.params.DHParameters;
import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentity;
import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentityManager;
import org.openecard.bouncycastle.crypto.tls.TlsPSKKeyExchange;
import org.openecard.crypto.common.keystore.KeyLengthException;
import org.openecard.crypto.common.keystore.KeyTools;


/**
 * KeyExchange class based on the BC counterpart with an additional check on the key strength.
 *
 * @author Tobias Wich
 */
public class TlsPSKKeyExchangeStrengthCheck extends TlsPSKKeyExchange {

    public TlsPSKKeyExchangeStrengthCheck(int keyExchange, Vector supportedSigAlgs, TlsPSKIdentity pskIdentity,
	    TlsPSKIdentityManager pskIdentityManager, DHParameters dhParams, int[] namedCurves,
	    short[] clientECPointFormats, short[] serverECPointFormats) {
	super(keyExchange, supportedSigAlgs, pskIdentity, pskIdentityManager, dhParams, namedCurves,
		clientECPointFormats, serverECPointFormats);
    }

    @Override
    public void processServerKeyExchange(InputStream input) throws IOException {
	super.processServerKeyExchange(input);
	try {
	    if (dhAgreePublicKey != null) {
		KeyTools.assertKeyLength(dhAgreePublicKey);
	    } else if (ecAgreePublicKey != null) {
		KeyTools.assertKeyLength(ecAgreePublicKey);
	    }
	} catch (KeyLengthException ex) {
	    throw new IOException(ex);
	}
    }

}
