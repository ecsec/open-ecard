/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.sal.util;

import org.openecard.common.util.ByteUtils;

/**
 *
 * @author Neil Crossley
 */
public class HexShim {

    private final byte[] value;

    public HexShim(byte[] value) {
	this.value = value;
    }

    @Override
    public String toString() {
	return ByteUtils.toHexString(value);
    }
}
