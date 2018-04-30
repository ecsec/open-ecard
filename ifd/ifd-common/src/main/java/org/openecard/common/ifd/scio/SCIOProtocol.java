/****************************************************************************
 * Copyright (C) 2015-2017 ecsec GmbH.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.ECardConstants;


/**
 * ISO/IEC card protocol types.
 *
 * @author Tobias Wich
 */
public enum SCIOProtocol {

    /**
     * Byte oriented T=0 protocol.
     */
    T0("T=0"),
    /**
     * Block oriented T=1 protocol.
     */
    T1("T=1"),
    /**
     * Contactless protocol.
     */
    TCL("T=CL"),
    /**
     * Any protocol.
     * This value may be used to connect cards and to indicate some unkown protocol type.
     */
    ANY("*");

    public final String identifier;

    private SCIOProtocol(String identifier) {
	this.identifier = identifier;
    }

    /**
     * Gets the element matching the given protocol.
     * If the protocol is not known or can not be determined, {@link ANY} is returned.
     *
     * @param protocol The protocol string to translate to the enum.
     * @return The enum closest to representing the given protocol string.
     */
    @Nonnull
    public static SCIOProtocol getType(@Nullable String protocol) {
	if (T0.identifier.equals(protocol)) {
	    return T0;
	} else if (T1.identifier.equals(protocol)) {
	    return T1;
	} else if (TCL.identifier.equals(protocol)) {
	    return TCL;
	} else {
	    return ANY;
	}
    }

    @Override
    public String toString() {
	return identifier;
    }

    @Nullable
    public String toUri() {
	switch (this) {
	    case T0:
		return ECardConstants.IFD.Protocol.T0;
	    case T1:
		return ECardConstants.IFD.Protocol.T1;
	    case TCL:
		return ECardConstants.IFD.Protocol.TYPE_A;
	    default:
		// no distinct protocol known
		return null;
	}
    }

}
