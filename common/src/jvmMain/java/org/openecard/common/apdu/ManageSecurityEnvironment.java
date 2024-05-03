/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.common.apdu;

import org.openecard.common.apdu.common.CardCommandAPDU;


/**
 * MANAGE SECURITY ENVIRONMENT command.
 * See ISO/IEC 7816-4 Section 7.5.11.
 *
 * @author Moritz Horsch
 */
public class ManageSecurityEnvironment extends CardCommandAPDU {

    /** MANAGE SECURITY ENVIRONMENT command instruction byte. */
    private static final byte COMMMAND_MSESet_AT = (byte) 0x22;
    /** Control reference template for authentication. */
    public static final byte AT = (byte) 0xA4;
    /** Control reference template for key agreement. */
    public static final byte KAT = (byte) 0xA6;
    /** Control reference template for hash-code. */
    public static final byte HT = (byte) 0xAA;
    /** Control reference template for cryptographic checksum. */
    public static final byte CCT = (byte) 0xB4;
    /** Control reference template for digital signature. */
    public static final byte DST = (byte) 0xB6;
    /** Control reference template for confidentiality. */
    public static final byte CT = (byte) 0xB8;

    /**
     * Creates a new MANAGE SECURITY ENVIRONMENT APDU.
     *
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     */
    protected ManageSecurityEnvironment(byte p1, byte p2) {
	super(x00, COMMMAND_MSESet_AT, p1, p2);
    }

    /**
     * Creates a new MANAGE SECURITY ENVIRONMENT APDU.
     *
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data
     */
    public ManageSecurityEnvironment(byte p1, byte p2, byte[] data) {
	super(x00, COMMMAND_MSESet_AT, p1, p2);
	setData(data);
    }

    public static class Set extends ManageSecurityEnvironment {

	public Set(byte p1, byte p2) {
	    super(p1, p2);
	}
    }

    public static class Store extends ManageSecurityEnvironment {

	public Store(byte p2) {
	    super((byte) 0xF2, p2);
	}
    }

    public static class Restore extends ManageSecurityEnvironment {

	public Restore(byte p2) {
	    super((byte) 0xF3, p2);
	}
    }

    public static class Erase extends ManageSecurityEnvironment {

	public Erase(byte p2) {
	    super((byte) 0xF4, p2);
	}
    }

}
