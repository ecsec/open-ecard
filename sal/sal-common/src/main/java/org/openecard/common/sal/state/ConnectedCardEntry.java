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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
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

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("ConnectedCardEntry={");
	this.toString(builder);
	builder.append("}");
	return builder.toString();
    }

    @Override
    protected void toString(StringBuilder builder) {
	builder.append("slotHandle=");
	builder.append(ByteUtils.toHexString(slotHandle));
	builder.append(", ");
	super.toString(builder);
    }

    @Override
    public void fillConnectionHandle(ConnectionHandleType connectionHandle) {
	super.fillConnectionHandle(connectionHandle);
	connectionHandle.setSlotHandle(slotHandle);
    }


}
