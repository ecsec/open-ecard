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

import android.content.Context;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.android.eac.EacNavigator;
import org.openecard.gui.definition.UserConsentDescription;

/**
 *
 * @author Neil Crossley
 */
public class EacNavigatorFactory implements UserConsentNavigatorFactory{

    @Override
    public boolean canCreateFrom(UserConsentDescription uc, Context androidCtx) {
	return "EAC".equals(uc.getDialogType());
    }

    @Override
    public UserConsentNavigator createFrom(UserConsentDescription uc, Context androidCtx) {
	if (!this.canCreateFrom(uc, androidCtx)) {
	    throw new IllegalArgumentException("This factory explicitly does not support the given user consent description.");
	}
	return EacNavigator.createFrom(androidCtx, uc);
    }
    
}
