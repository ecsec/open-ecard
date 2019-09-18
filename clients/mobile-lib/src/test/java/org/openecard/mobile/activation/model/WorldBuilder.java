/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import org.openecard.mobile.activation.common.CommonActivationUtils;

/**
 *
 * @author Neil Crossley
 */
public class WorldBuilder implements Builder<World> {

    private final OpeneCardContextConfigFactory configFactory;
    private final MockNFCCapabilitiesConfigurator capabilities;
    private final MockMobileTerminalConfigurator terminalConfigurator;

    public WorldBuilder(OpeneCardContextConfigFactory configFactory, MockNFCCapabilitiesConfigurator capabilities, MockMobileTerminalConfigurator terminalConfigurator) {
	this.configFactory = configFactory;
	this.capabilities = capabilities;
	this.terminalConfigurator = terminalConfigurator;
    }

    public World build() {
	CommonActivationUtils activationUtils = new CommonActivationUtils(this.configFactory.build());
	return new World(activationUtils, capabilities, terminalConfigurator);
    }

    public static WorldBuilder create() {
	MockMobileTerminalConfigurator terminalConfigurator = MockMobileTerminalConfigurator.instance();
	return new WorldBuilder(
		OpeneCardContextConfigFactory.mobile(terminalConfigurator.build()),
		MockNFCCapabilitiesConfigurator.createWithFullNfc(),
		terminalConfigurator);
    }

    public WorldBuilder withMobileNfcStack() {
	terminalConfigurator.withMobileNfcStack();
	return this;
    }
}
