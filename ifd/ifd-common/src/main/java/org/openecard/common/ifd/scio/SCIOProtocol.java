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
     * This value may be used to connect cards and to indicate some unkown protocl type.
     */
    ANY("*");

    private final String identifier;

    private SCIOProtocol(String identifier) {
	this.identifier = identifier;
    }

    @Override
    public String toString() {
	return identifier;
    }

}
