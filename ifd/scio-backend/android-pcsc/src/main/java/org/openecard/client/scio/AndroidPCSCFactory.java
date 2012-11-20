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

import android.content.Context;
import javax.smartcardio.CardTerminals;
import org.openecard.client.common.ifd.AndroidTerminalFactory;
import sun.security.smartcardio.PCSC;
import sun.security.smartcardio.PCSCException;
import sun.security.smartcardio.PCSCTerminals;


/**
 * TerminalFactory for PC/SC on Android.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class AndroidPCSCFactory implements AndroidTerminalFactory {

    @Override
    public String getType() {
	return "Android PC/SC factory";
    }

    @Override
    public CardTerminals terminals() {
	PCSC.checkAvailable();
	try {
	    PCSCTerminals.initContext();
	} catch (PCSCException e) {
	    // TODO log
	    e.printStackTrace();
	}
	return new PCSCTerminals();
    }

    @Override
    public void stop() {
	RootHelper.killPCSCD();
    }

    @Override
    public void start(Object o) {
	Context c = (Context) o;
	ResourceUnpacker.unpackResources(c);
	RootHelper.startPCSCD(c.getFilesDir());
    }

}
