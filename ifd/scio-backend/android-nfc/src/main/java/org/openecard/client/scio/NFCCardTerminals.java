/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.scio;

import java.util.ArrayList;
import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;


/**
 * NFC implementation of smartcardio's CardTerminals interface.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class NFCCardTerminals extends CardTerminals {

    @Override
    public List<CardTerminal> list(State arg0) throws CardException {
	List<CardTerminal> list = new ArrayList<CardTerminal>();
	list.add(NFCCardTerminal.getInstance());
	return list;
    }

    @Override
    public boolean waitForChange(long arg0) throws CardException {
	// TODO Auto-generated method stub
	return false;
    }

}
