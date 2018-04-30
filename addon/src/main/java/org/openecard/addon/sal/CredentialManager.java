/****************************************************************************
 * Copyright (C) 2014-2017 ecsec GmbH.
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

package org.openecard.addon.sal;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The class implements a Credential manager which allows to add, get and remove credentials from a {@link CardStateMap}.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class CredentialManager {

    /**
     * Logger for writing log messages.
     */
    private static final Logger logger = LoggerFactory.getLogger(CredentialManager.class);

    /**
     * CardStateMap which contains all credentials.
     */
    private final CardStateMap states;

    /**
     * The constructor sets the initial value of the internal managed {@link CardStateMap}.
     *
     * @param states An initial {@link CardStateMap} to set.
     */
    public CredentialManager(CardStateMap states) {
	this.states = states;
    }

    /**
     * The method adds a new credential to the managed {@link CardStateMap}.
     *
     * @param handle A {@link ConnectionHandleType} object for the creation of a new {@link CardStateEntry} object.
     * @param protocol Interface protocol with which the card is connected.
     * @param cif A {@link CardInfoType} object for the creation of a new {@link CardStateEntry} object.
     * @return The method returns {@code true} if the credential was added successfully else {@code false}.
     */
    public boolean addCredential(ConnectionHandleType handle, String protocol, CardInfoType cif) {
	if (handle == null || cif == null) {
	    logger.warn("The ConnectionHandle and/or CardInfo object is null. Can't add the Credential.");
	    return false;
	}

	CardStateEntry entry = new CardStateEntry(handle, cif, protocol);
	states.addEntry(entry);
	return true;
    }

    /**
     * The method removes a credential from the managed {@link CardStateMap}.
     *
     * @param handle A {@link ConnectionHandleType} object identifying the credential.
     * @return The method returns {@code true} if the credential was successfully removed else {@code false} .
     */
    public boolean removeCredential(ConnectionHandleType handle) {
	if (handle == null) {
	    logger.warn("The ConnectionHandle is null. Can't remove the requested Credential.");
	    return false;
	}

	states.removeEntry(handle);
	return true;
    }

}
