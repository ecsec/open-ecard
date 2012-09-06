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
package org.openecard.client.common.tlv;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public enum TagClass {

    UNIVERSAL((byte)0), APPLICATION((byte)1), CONTEXT((byte)2), PRIVATE((byte)3);

    public final byte num;

    private TagClass(byte num) {
	this.num = num;
    }

    public static TagClass getTagClass(byte octet) {
	byte classByte = (byte) ((octet >> 6) & 0x03);
	switch (classByte) {
	case 0: return UNIVERSAL;
	case 1: return APPLICATION;
	case 2: return CONTEXT;
	case 3: return PRIVATE;
	default: return null; // what possible values are there in 2 bits?!?
	}
    }

}
