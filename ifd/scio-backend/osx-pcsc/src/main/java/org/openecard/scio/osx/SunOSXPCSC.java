/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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

package org.openecard.scio.osx;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactorySpi;


/**
 * OS X PC/SC provider.
 * For more information see {@link package-info}.
 *
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public final class SunOSXPCSC extends Provider {

    private static final long serialVersionUID = -8720099822090870310L;

    /**
     * Default constructor registering this provider.
     */
    public SunOSXPCSC() {
	super("SunOSXPCSC", 1.0d, "Sun OS X PC/SC provider");
	AccessController.doPrivileged(new PrivilegedAction<Void>() {
	    public Void run() {
		put("TerminalFactory.PC/SC", "org.openecard.scio.osx.SunOSXPCSC$Factory");
		return null;
	    }
	});
    }

    /**
     * Internal provider factory.
     * For more information see {@link TerminalFactorySpi}
     *
     * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
     */
    public static final class Factory extends TerminalFactorySpi {
	/**
	 * Constructor checking availability & initializing context.
	 *
	 * @param obj provider parameters
	 * @throws PCSCException if problems occur.
	 */
	public Factory(Object obj) throws PCSCException {
	    if (obj != null) {
		throw new IllegalArgumentException("SunOSXPCSC factory doesn't take parameters");
	    }
	    PCSC.checkAvailable();
	    PCSCTerminals.initContext();
	}

	/**
	 * Returns the available readers. This must be a new object for each call.
	 *
	 * @return CardTerminals card terminals wrapper object
	 */
	@Override
	protected CardTerminals engineTerminals() {
	    return new PCSCTerminals();
	}
    }

}
