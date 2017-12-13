/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.android.ex;

import static org.openecard.android.system.ServiceMessages.*;


/**
 * Is thrown if the corresponding device doesn't support nfc.
 *
 * @author Mike Prechtl
 */
public class NfcUnavailable extends Exception {

    private static final String STD_ERROR_MESSAGE = NFC_NOT_AVAILABLE_FAIL;

    public NfcUnavailable() {
	super(STD_ERROR_MESSAGE);
    }

    public NfcUnavailable(String message) {
	super(message);
    }

    public NfcUnavailable(String message, Throwable cause) {
	super(message, cause);
    }

    public NfcUnavailable(Throwable cause) {
	super(STD_ERROR_MESSAGE, cause);
    }

}
