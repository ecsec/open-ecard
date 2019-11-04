/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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

import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.mobile.pinmanagement.PINManagementGuiImpl;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.mobile.GuiIfaceReceiver;
import org.openecard.mobile.activation.PinManagementInteraction;


/**
 *
 * @author Sebastian Schuberth
 */
public class PINManagementNavigatorFactory implements UserConsentNavigatorFactory<PinManagementInteraction> {

    private final GuiIfaceReceiver<PINManagementGuiImpl> ifaceReceiver = new GuiIfaceReceiver<>();

    public static final String PROTOCOL_TYPE = "PIN-Management";

    @Override
    public String getProtocolType() {
	return PROTOCOL_TYPE;
    }

    @Override
    public boolean canCreateFrom(UserConsentDescription uc) {
	return "pin_change_dialog".equals(uc.getDialogType());
    }

    @Override
    public UserConsentNavigator createFrom(UserConsentDescription uc) {
	if (! this.canCreateFrom(uc)) {
	    throw new IllegalArgumentException("This factory explicitly does not support the given user consent description.");
	}

	ifaceReceiver.setUiInterface(new PINManagementGuiImpl());
	return new PINManagementNavigator(uc, ifaceReceiver);
    }

    @Override
    public void setInteractionComponent(PinManagementInteraction interaction) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
