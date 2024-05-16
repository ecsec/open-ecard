/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.common.ifd.scio;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;


/**
 * Class representing the state of a terminal.
 * In contrast to {@link SCIOTerminal}, this class does not alter its state according to the the underlying hardware.
 * It is therefore a snapshot of the state of a terminal at a given point in time.
 *
 * @author Tobias Wich
 */
public class TerminalState {

    private final String name;
    private final boolean cardPresent;

    public TerminalState(String name, boolean cardPresent) {
	this.name = name;
	this.cardPresent = cardPresent;
    }

    public TerminalState(SCIOTerminal term) throws SCIOException {
	this.name = term.getName();
	this.cardPresent = term.isCardPresent();
    }

    public String getName() {
	return name;
    }

    public boolean isCardPresent() {
	return cardPresent;
    }

    @Nonnull
    public static List<TerminalState> convert(@Nonnull List<SCIOTerminal> terminals) throws SCIOException {
	ArrayList<TerminalState> result = new ArrayList<>(terminals.size());
	for (SCIOTerminal next : terminals) {
	    result.add(new TerminalState(next));
	}
	return result;
    }

}
