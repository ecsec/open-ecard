/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.event;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import javax.annotation.Nonnull;
import org.openecard.common.event.EventObject;
import org.openecard.common.util.ByteUtils;
import org.openecard.mdlw.sal.MwSlot;


/**
 *
 * @author Tobias Wich
 */
public class MwEventObject extends EventObject {

    private final MwSlot slot;

    public MwEventObject(@Nonnull ConnectionHandleType handle, MwSlot slot) {
	super(handle);
	this.slot = slot;
    }

    public MwSlot getMwSlot() {
	return slot;
    }

    @Override
    @Nonnull
    @SuppressWarnings("null")
    public ConnectionHandleType getHandle() {
	return super.getHandle();
    }

    @Override
    public String toString() {
	ConnectionHandleType handle = getHandle();
	String ifdName = handle.getIFDName();
	String slotHandle = ByteUtils.toHexString(handle.getSlotHandle());
	ConnectionHandleType.RecognitionInfo recInfo = handle.getRecognitionInfo();
	String cardType = recInfo != null ? recInfo.getCardType() : null;
	return String.format("MwEventObject={ifdName=%s, slot=%s, cardType=%s}", ifdName, slotHandle, cardType);
    }

}
