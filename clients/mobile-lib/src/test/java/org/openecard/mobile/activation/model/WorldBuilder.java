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

import org.openecard.mobile.activation.common.CommonActivationUtils;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;

/**
 *
 * @author Neil Crossley
 */
public class WorldBuilder implements Builder<World> {

    private final OpeneCardContextConfigFactory configFactory;
    private final MockNFCCapabilitiesConfigurator capabilities;
    private final MobileTerminalConfigurator terminalConfigurator;

    public WorldBuilder(OpeneCardContextConfigFactory configFactory, MockNFCCapabilitiesConfigurator capabilities, MobileTerminalConfigurator terminalConfigurator) {
	this.configFactory = configFactory;
	this.capabilities = capabilities;
	this.terminalConfigurator = terminalConfigurator;
    }

    public World build() {
	CommonActivationUtils activationUtils = new CommonActivationUtils(this.configFactory.build(), new NFCDialogMsgSetter() {
	    @Override
	    public void setText(String msg) {
	    }

	    @Override
	    public boolean isSupported() {
		return true;
	    }
	});
	return new World(activationUtils, capabilities, terminalConfigurator);
    }

    public static WorldBuilder create() {
	MobileTerminalConfigurator terminalConfigurator = MobileTerminalConfigurator.withMobileNfcStack();
	return new WorldBuilder(
		OpeneCardContextConfigFactory.mobile(terminalConfigurator.build()),
		MockNFCCapabilitiesConfigurator.createWithFullNfc(),
		terminalConfigurator);
    }
}
