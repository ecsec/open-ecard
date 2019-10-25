/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.sal.state;

import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich
 */
public class ConnectedCardEntry extends CardEntry {

    protected byte[] slotHandle;

    public ConnectedCardEntry(byte[] slotHandle, CardEntry base) {
	super(base.ctxHandle, base.ifdName, base.slotIdx, base.cif);
	this.slotHandle = ByteUtils.clone(slotHandle);
    }

}
