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
import org.openecard.android.utils.NfcCapabilityHelper;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.NfcCapabilityResult;

/**
 *
 * @author Neil Crossley
 */
public class AndroidNfcCapabilities implements NFCCapabilities  {

    private final NfcCapabilityHelper capabilityHelper;

    AndroidNfcCapabilities(NfcCapabilityHelper capabilityHelper) {
	this.capabilityHelper = capabilityHelper;
    }

    @Override
    public boolean isAvailable() {
	return capabilityHelper.isNFCAvailable();
    }

    @Override
    public boolean isEnabled() {
	return capabilityHelper.isNFCEnabled();
    }

    @Override
    public NfcCapabilityResult checkExtendedLength() {
	return capabilityHelper.checkExtendedLength();
    }

    public static AndroidNfcCapabilities create(Context context) {
	return new AndroidNfcCapabilities(NfcCapabilityHelper.create(context));
    }

}
