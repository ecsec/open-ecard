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

import org.openecard.common.interfaces.Dispatcher;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;


/**
 *
 * @author Neil Crossley
 */
public class EacNavigatorFactory implements UserConsentNavigatorFactory<EacInteraction> {

    private EacInteraction interaction;
    private NFCDialogMsgSetter msgSetter;
    private Dispatcher dispatcher;

    public static final String PROTOCOL_TYPE = "EAC";

    public void setDialogMsgSetter(NFCDialogMsgSetter msgSetter) {
	this.msgSetter = msgSetter;
    }

    private void setDispatcher(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public String getProtocolType() {
	return PROTOCOL_TYPE;
    }

    @Override
    public boolean canCreateFrom(UserConsentDescription uc) {
	return "EAC".equals(uc.getDialogType());
    }

    @Override
    public UserConsentNavigator createFrom(UserConsentDescription uc) {
	if (! this.canCreateFrom(uc)) {
	    throw new IllegalArgumentException("This factory explicitly does not support the given user consent description.");
	}

	return new EacNavigator(uc, interaction, msgSetter, dispatcher);
    }

    @Override
    public void setInteractionComponent(EacInteraction interaction) {
	this.interaction = interaction;
    }

    public static EacNavigatorFactory create(NFCDialogMsgSetter msgSetter, Dispatcher dispatcher) {
	EacNavigatorFactory factory = new EacNavigatorFactory();
	factory.setDialogMsgSetter(msgSetter);
	factory.setDispatcher(dispatcher);
	return factory;
    }

}
