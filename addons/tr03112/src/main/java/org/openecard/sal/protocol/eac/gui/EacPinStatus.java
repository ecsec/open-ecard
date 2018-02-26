/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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

package org.openecard.sal.protocol.eac.gui;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.util.Arrays;


/**
 *
 * @author Tobias Wich
 */
public enum EacPinStatus {

    /**
     * PIN activated, 3 tries left.
     */
    RC3(new byte[] {(byte) 0x90, (byte) 0x00}),
    /**
     * PIN activated, 2 tries left.
     */
    RC2(new byte[] {(byte) 0x63, (byte) 0xC2}),
    /**
     * PIN suspended, 1 try left, CAN needs to be entered.
     */
    RC1(new byte[] {(byte) 0x63, (byte) 0xC1}),
    /**
     * PIN blocked, 0 tries left.
     */
    BLOCKED(new byte[] {(byte) 0x63, (byte) 0xC0}),
    /**
     * PIN/Card not activated.
     */
    DEACTIVATED(new byte[] {(byte) 0x62, (byte) 0x83});

    private final byte[] code;

    private EacPinStatus(byte[] code) {
	this.code = code;
    }

    public byte[] getCode() {
	return Arrays.clone(code);
    }

    public static List<byte[]> getCodes() {
	ArrayList<byte[]> result = new ArrayList<>(values().length);
	for (EacPinStatus ps : values()) {
	    result.add(ps.getCode());
	}
	return result;
    }

    /**
     * Gets the PIN status for the given response code.
     *
     * @param code PIN status code as returned by the PIN status APDU.
     * @return The PIN status matching the given code.
     * @throws IllegalArgumentException Thrown in case the given code is not a specified status code.
     */
    @Nonnull
    public static EacPinStatus fromCode(byte[] code) throws IllegalArgumentException {
	for (EacPinStatus ps : values()) {
	    if (java.util.Arrays.equals(ps.code, code)) {
		return ps;
	    }
	}
	// no status found
	throw new IllegalArgumentException("The given value is not a valid PIN status code.");
    }

}
