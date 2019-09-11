/****************************************************************************
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
 ***************************************************************************/

package org.openecard.mobile.activation.model;

import org.openecard.mobile.activation.NfcCapabilityResult;

/**
 *
 * @author Neil Crossley
 */
public class NfcConfig {

    public boolean isAvailable;
    public boolean isEnabled;
    public NfcCapabilityResult checkExtendedLength;

    public NfcConfig(boolean isAvailable, boolean isEnabled, NfcCapabilityResult checkExtendedLength) {
	this.isAvailable = isAvailable;
	this.isEnabled = isEnabled;
	this.checkExtendedLength = checkExtendedLength;
    }

    public static NfcConfig create() {
	return new NfcConfig(true, true, NfcCapabilityResult.SUPPORTED);
    }

    public static NfcConfig createUnavailable() {
	return new NfcConfig(false, true, NfcCapabilityResult.SUPPORTED);
    }
}
