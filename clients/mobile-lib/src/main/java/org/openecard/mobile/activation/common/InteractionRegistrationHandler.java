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
package org.openecard.mobile.activation.common;

import org.openecard.mobile.activation.ActivationInteraction;
import org.openecard.mobile.system.OpeneCardContext;
import org.openecard.mobile.ui.UserConsentNavigatorFactory;


/**
 *
 * @author Neil Crossley
 */
public class InteractionRegistrationHandler {

    public static <T extends ActivationInteraction> AutoCloseable hookUp(String protocolType, OpeneCardContext context, T interaction) {
	UserConsentNavigatorFactory<T> fac = context.getGuiNavigatorFactory(protocolType);
	fac.setInteractionComponent(interaction);

	return new AutoCloseable() {
	    @Override
	    public void close() throws Exception {
		fac.setInteractionComponent(null);
	    }
	};
    }

}
