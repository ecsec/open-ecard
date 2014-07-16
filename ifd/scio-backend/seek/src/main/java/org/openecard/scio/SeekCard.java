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

package org.openecard.scio;

import java.io.IOException;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOException;
import org.simalliance.openmobileapi.Session;


/**
 * Seek implementation of smartcardio's Card interface.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SeekCard implements SCIOCard {

    private static byte[] aid = null;
    private Session session;

    /*
     * SELECT-command is not allowed in seek, so we must set the aid beforhand
     * and use it in getbasicchannel and openlogicalchannel. Cant pass it there
     * because the smartcardios interface doesnt declare parameters for that
     * functions.
     */
    public static void setAID(byte[] b) {
	aid = b;
    }

    public SeekCard(Session s) {
	this.session = s;
    }

    @Override
    public void beginExclusive() throws SCIOException {
	// TODO
    }

    @Override
    public void disconnect(boolean arg0) throws SCIOException {
	this.session.close();
    }

    @Override
    public void endExclusive() throws SCIOException {
	// TODO
    }

    @Override
    public SCIOATR getATR() {
	return new SCIOATR(this.session.getATR());
    }

    @Override
    public SCIOChannel getBasicChannel() {
	try {
	    return new SeekChannel(this.session.openBasicChannel(aid));
	} catch (IOException e) {
	    return null;
	}
    }

    @Override
    public String getProtocol() {
	/* for now theres no way to get the used protocol in seek */
	return "";
    }

    @Override
    public SCIOChannel openLogicalChannel() throws SCIOException {
	try {
	    return new SeekChannel(this.session.openLogicalChannel(aid));
	} catch (IOException e) {
	    throw new SCIOException(e);
	}
    }

    @Override
    public byte[] transmitControlCommand(int arg0, byte[] arg1) throws SCIOException {
	return new byte[0];
    }

}
