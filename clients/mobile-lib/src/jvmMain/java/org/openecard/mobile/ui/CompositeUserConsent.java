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

package org.openecard.mobile.ui;

import java.util.List;
import org.openecard.gui.FileDialog;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;


/**
 *
 * @author Tobias Wich
 */
public class CompositeUserConsent implements UserConsent {

    private final List<UserConsentNavigatorFactory<?>> factories;
    private final MessageDialog messageDialog;

    public CompositeUserConsent(List<UserConsentNavigatorFactory<?>> factories, MessageDialog messageDialog) {
	this.factories = factories;
	this.messageDialog = messageDialog;
    }

    @Override
    public UserConsentNavigator obtainNavigator(UserConsentDescription uc) {

	for (UserConsentNavigatorFactory factory : factories) {
	    if(factory.canCreateFrom(uc)) {
		UserConsentNavigator nav = factory.createFrom(uc);
		return nav;
	    }
	}

	throw new UnsupportedOperationException("Unsupported UserConsent type.");
    }

    @Override
    public FileDialog obtainFileDialog() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDialog obtainMessageDialog() {
	return this.messageDialog;
    }

}
