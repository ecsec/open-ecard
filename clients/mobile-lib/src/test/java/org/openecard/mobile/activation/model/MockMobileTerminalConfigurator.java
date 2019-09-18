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

import static org.mockito.Mockito.*;
import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.scio.NFCCardTerminal;
import org.openecard.scio.NFCCardTerminals;

/**
 *
 * @author Neil Crossley
 */
public class MockMobileTerminalConfigurator implements Builder<TerminalFactory> {

    private final TerminalFactory mockTerminalFactory;
    private NFCCardTerminal terminal;
    private NFCCardTerminals terminals;

    public MockMobileTerminalConfigurator() {
	this.mockTerminalFactory = mock(TerminalFactory.class);
    }

    @Override
    public TerminalFactory build() {
	return mockTerminalFactory;
    }

    public static MockMobileTerminalConfigurator instance() {
	return new MockMobileTerminalConfigurator();
    }

    MockMobileTerminalConfigurator withMobileNfcStack() {
	this.terminal = new NFCCardTerminal();
	this.terminals = new NFCCardTerminals(terminal);
	when(mockTerminalFactory.terminals()).thenReturn(this.terminals);
	return this;
    }
}
