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
import java.util.List;
import org.openecard.gui.FileDialog;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.android.stub.MessageDialogStub;
import org.openecard.gui.definition.UserConsentDescription;


/**
 *
 * @author Tobias Wich
 */
public class AndroidUserConsent implements UserConsent {

    private final Context androidCtx;
    private final List<UserConsentNavigatorFactory> factories;

    public AndroidUserConsent(Context androidCtx, List<UserConsentNavigatorFactory> factories) {
	this.androidCtx = androidCtx;
	this.factories = factories;
    }

    @Override
    public UserConsentNavigator obtainNavigator(UserConsentDescription uc) {
	
	for (UserConsentNavigatorFactory factory : factories) {
	    if(factory.canCreateFrom(uc, androidCtx))
	    {
		return factory.createFrom(uc, androidCtx);
	    }
	}
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileDialog obtainFileDialog() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialog obtainMessageDialog() {
	return new MessageDialogStub(); // return stub object
    }

}
