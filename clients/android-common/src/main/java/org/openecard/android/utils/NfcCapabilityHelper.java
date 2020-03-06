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
package org.openecard.android.utils;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import org.openecard.mobile.activation.NfcCapabilityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class NfcCapabilityHelper<T extends Context> {

    public static Logger LOG = LoggerFactory.getLogger(NfcCapabilityHelper.class);

    private final T activity;
    private final NfcAdapter nfcAdapter;

    NfcCapabilityHelper(T activity, NfcAdapter nfcAdapter) {
	if (activity == null) {
	    throw new IllegalArgumentException("activity cannot be null");
	}
	this.activity = activity;
	this.nfcAdapter = nfcAdapter;
    }

    public T getContext() {
	return activity;
    }

    public NfcAdapter getNfcAdapter() {
	return nfcAdapter;
    }

    public static <T extends Context> NfcCapabilityHelper<T> create(T activity) {
	if (activity == null) {
	    throw new IllegalArgumentException("activity cannot be null");
	}
	NfcManager nfcManager = (NfcManager) activity.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = nfcManager.getDefaultAdapter();

	return new NfcCapabilityHelper(activity, adapter);
    }

    /*
     * Check if NFC is available on the corresponding device.
     *
     * @return true if nfc is available, otherwise false
     */
    public boolean isNFCAvailable() {
       return nfcAdapter != null;
    }

    /**
     * Proof if NFC is enabled on the corresponding device. If this method return {@code false} nfc should be activated
     * in the device settings.
     *
     * @return true if nfc is enabled, otherwise false
     */
    public boolean isNFCEnabled() {
	return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    public NfcCapabilityResult checkExtendedLength() {
	return NfcExtendedHelper.checkExtendedLength(activity);
    }

}
