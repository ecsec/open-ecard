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

package org.openecard.common.tlv.iso7816;


/**
 * See ISO/IEC 7816-4 p.21 tab. 14
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class FileDescriptorByte {

    private final byte data;

    public FileDescriptorByte(byte data) {
	this.data = data;
    }


    // test msb
    private boolean isFD() {
	return ((data >> 7) & 0x01) == 1 ? false : true;
    }


    public boolean shareable() {
	if (isFD()) {
	    // bit 7
	    return ((data >> 6) & 0x01) == 1 ? true : false;
	}
	return false;
    }

    public boolean isDF() {
	if (isFD()) {
	    // 111000
	    return (data & 0x38) == 0x38;
	}
	return false;
    }

    public boolean isWorkingEF() {
	if (isFD()) {
	    return ((data >> 3) & 0x07) == 0;
	}
	return false;
    }
    public boolean isInternalEF() {
	if (isFD()) {
	    return ((data >> 3) & 0x07) == 1;
	}
	return false;
    }
    public boolean isProprietaryEF() {
	if (isFD()) {
	    byte val = (byte) ((data >> 3) & 0x07);
	    return val < 0x07 && val != 0 && val != 1;
	}
	return false;
    }

    public boolean isEF() {
	return isWorkingEF() || isInternalEF() || isProprietaryEF();
    }


    public boolean isUnknownFormat() {
	if (isEF() && ((data & 0x07) == 0x00)) {
	    return true;
	}
	return false;
    }
    public boolean isTransparent() {
	if (isEF() && ((data & 0x07) == 0x01)) {
	    return true;
	}
	return false;
    }
    public boolean isLinear() {
	if (isEF()) {
	    byte lower = (byte) (data & 0x07);
	    if (lower == 2 || lower == 3 || lower == 4 || lower == 5) {
		return true;
	    }
	}
	return false;
    }
    public boolean isCyclic() {
	if (isEF()) {
	    byte lower = (byte) (data & 0x07);
	    if (lower == 6 || lower == 7) {
		return true;
	    }
	}
	return false;
    }


    public boolean isDataObject() {
	// 0X111010 || 0X111001
	if (isFD() && ((data >> 3) & 0x07) == 0x07) {
	    byte lower = (byte) (data & 0x07);
	    if (lower == 1 || lower == 2) {
		return true;
	    }
	}
	return false;
    }


    public String toString(String prefix) {
	StringBuilder b = new StringBuilder(4096);
	b.append(prefix);
	b.append("FileDescriptor-Byte: ");
	if (shareable()) {
	    b.append("shareable ");
	}
	if (isDF()) {
	    b.append("DF ");
	}
	if (isEF()) {
	    b.append("EF ");
	}
	if (isInternalEF()) {
	    b.append("internal ");
	}
	if (isWorkingEF()) {
	    b.append("working ");
	}
	if (isProprietaryEF()) {
	    b.append("proprietary ");
	}
	if (isUnknownFormat()) {
	    b.append("unknown-format ");
	}
	if (isTransparent()) {
	    b.append("transparent ");
	}
	if (isLinear()) {
	    b.append("linear ");
	}
	if (isCyclic()) {
	    b.append("cyclic ");
	}
	if (isDataObject()) {
	    b.append("data-object ");
	}

	return b.toString();
    }

    @Override
    public String toString() {
	return toString("");
    }

}
