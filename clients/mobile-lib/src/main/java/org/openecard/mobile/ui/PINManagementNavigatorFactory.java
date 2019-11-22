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

import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.mobile.activation.PinManagementInteraction;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;


/**
 *
 * @author Sebastian Schuberth
 */
public class PINManagementNavigatorFactory implements UserConsentNavigatorFactory<PinManagementInteraction> {

    public static final String PROTOCOL_TYPE = "PIN-Management";

    private PinManagementInteraction interaction;
    private final Dispatcher dispatcher;
    private final EventDispatcher eventDispatcher;
    private final NFCDialogMsgSetter msgSetter;

    public PINManagementNavigatorFactory(
	    Dispatcher dispatcher,
	    EventDispatcher eventDispatcher,
	    NFCDialogMsgSetter msgSetter) {
	this.dispatcher = dispatcher;
	this.eventDispatcher = eventDispatcher;
	this.msgSetter = msgSetter;
    }

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

	return new PINManagementNavigator(uc, interaction, dispatcher, eventDispatcher, msgSetter);
    }

    @Override
    public void setInteractionComponent(PinManagementInteraction interaction) {
	this.interaction = interaction;
    }

}
