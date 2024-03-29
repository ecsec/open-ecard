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

import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.scio.AbstractNFCCard;
import org.openecard.scio.NFCCardTerminal;
import org.openecard.scio.NFCCardTerminals;

/**
 *
 * @author Neil Crossley
 */
public class MobileTerminalConfigurator implements Builder<TerminalFactory> {

    public final TerminalFactory mockTerminalFactory;
    public final NFCCardTerminal terminal;
    public final NFCCardTerminals terminals;

    public MobileTerminalConfigurator(TerminalFactory mockTerminalFactory, NFCCardTerminal terminal, NFCCardTerminals terminals) {
	this.mockTerminalFactory = mockTerminalFactory;
	this.terminal = terminal;
	this.terminals = terminals;
    }

    @Override
    public TerminalFactory build() {
	return mockTerminalFactory;
    }

    public static MobileTerminalConfigurator withMobileNfcStack() {
	AbstractNFCCard mockNfcCard = mock(AbstractNFCCard.class);
	NFCCardTerminal mockTerminal = mock(NFCCardTerminal.class);
	FakeNFCCardTerminal fakeTerminalTerminal = new FakeNFCCardTerminal(mockTerminal);
	FakeNFCCard fakeNfcCard = new FakeNFCCard(mockNfcCard, fakeTerminalTerminal);
	NFCCardTerminals terminals = new NFCCardTerminals(fakeTerminalTerminal);
	TerminalFactory mockTerminalFactory = mock(TerminalFactory.class);

	try {
	    when(mockTerminalFactory.terminals()).thenReturn(terminals);
	    when(mockTerminal.prepareDevices()).thenAnswer(new Answer() {
		@Override
		public Object answer(InvocationOnMock invocation) throws Throwable {

		    fakeTerminalTerminal.setNFCCard(fakeNfcCard);

		    return true;
		}

	    });

	    return new MobileTerminalConfigurator(mockTerminalFactory, mockTerminal, terminals);
	} catch (SCIOException ex) {
	    throw new RuntimeException("Such an exception should not by thrown when initializing a mock.", ex);
	}
    }
}
