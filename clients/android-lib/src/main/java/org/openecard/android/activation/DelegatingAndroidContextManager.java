/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
 ************************************************************************** */
package org.openecard.android.activation;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import java.io.IOException;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.StartServiceHandler;
import org.openecard.mobile.activation.StopServiceHandler;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.scio.AndroidNFCFactory;
import org.openecard.scio.CachingTerminalFactoryBuilder;

/**
 *
 * @author Neil Crossley
 */
public class DelegatingAndroidContextManager implements AndroidContextManager {

    private final ContextManager contextManager;
    private final CachingTerminalFactoryBuilder<AndroidNFCFactory> builder;

    public DelegatingAndroidContextManager(ContextManager contextManager, CachingTerminalFactoryBuilder<AndroidNFCFactory> builder) {
	this.contextManager = contextManager;
	this.builder = builder;
    }

    @Override
    public void onNewIntent(Context context, Intent intent) throws ApduExtLengthNotSupported, IOException {
	AndroidNFCFactory nfcFactory = this.builder.getPreviousInstance();
	Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	if (nfcFactory != null && tagFromIntent != null) {
	    if (IsoDep.get(tagFromIntent).isExtendedLengthApduSupported()) {
		// set nfc tag with timeout of five seconds
		nfcFactory.setNFCTag(tagFromIntent, 5000);
	    } else {
		throw new ApduExtLengthNotSupported("APDU Extended Length is not supported.");
	    }
	}
    }

    @Override
    public void start(StartServiceHandler handler) throws UnableToInitialize, NfcUnavailable, NfcDisabled, ApduExtLengthNotSupported {
	this.contextManager.start(handler);
    }

    @Override
    public void stop(StopServiceHandler handler) {
	this.contextManager.stop(handler);
    }
}
