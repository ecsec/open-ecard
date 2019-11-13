/*
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
 */
package org.openecard.android.activation;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import org.openecard.android.utils.NfcUtils;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.NfcCapabilityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class AndroidNfcCapabilities implements NFCCapabilities  {

    private final Context context;

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNfcCapabilities.class);
    private NfcAdapter adapter;

    AndroidNfcCapabilities(Context context, NfcAdapter adapter) {
	this.context = context;
	this.adapter = adapter;
    }

    @Override
    public boolean isAvailable() {
	return adapter != null;
    }

    @Override
    public boolean isEnabled() {
	return adapter != null ? adapter.isEnabled() : false;
    }

    @Override
    public NfcCapabilityResult checkExtendedLength() {
	return NfcUtils.checkExtendedLength(this.context);
    }


    /**
     * Proof if NFC is available on the corresponding device.
     *
     * @return true if nfc is available, otherwise false
     */
    public boolean isNFCAvailable() {
	return adapter != null;
    }

    public static AndroidNfcCapabilities create(Context context) {
	NfcUtils.setContext(context);
	NfcAdapter adapter = getNfcAdapter(context);
	return new AndroidNfcCapabilities(context, adapter);
    }

    private static NfcAdapter getNfcAdapter(Context context) {
	NfcManager nfcManager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
	NfcAdapter adapter;
	if (nfcManager != null) {
	    adapter = nfcManager.getDefaultAdapter();
	} else {
	    adapter = null;
	}
	return adapter;
    }

}
