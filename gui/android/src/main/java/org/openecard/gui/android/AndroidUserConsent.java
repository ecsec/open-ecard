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

package org.openecard.gui.android;

import org.openecard.gui.android.eac.EacNavigator;
import android.content.Context;
import org.openecard.gui.FileDialog;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;


/**
 *
 * @author Tobias Wich
 */
public class AndroidUserConsent implements UserConsent {

    private final Context androidCtx;

    public AndroidUserConsent(Context androidCtx) {
	this.androidCtx = androidCtx;
    }

    @Override
    public UserConsentNavigator obtainNavigator(UserConsentDescription uc) {
	String dialogType = uc.getDialogType();
	if ("EAC".equals(dialogType)) {
	    return new EacNavigator(androidCtx, uc);
	} else if ("insert_card_dialog".equals(dialogType)) {
	    return new InsertCardNavigator(uc);
	} else {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    @Override
    public FileDialog obtainFileDialog() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialog obtainMessageDialog() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
