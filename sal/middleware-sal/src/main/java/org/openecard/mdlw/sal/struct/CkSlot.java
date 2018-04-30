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

package org.openecard.mdlw.sal.struct;

import java.nio.charset.StandardCharsets;
import org.openecard.mdlw.sal.cryptoki.CK_SLOT_INFO;
import org.openecard.mdlw.sal.cryptoki.CK_VERSION;


/**
 *
 * @author Tobias Wich
 */
public class CkSlot {

    private final CK_SLOT_INFO orig;
    private final long slotID;

    public CkSlot(CK_SLOT_INFO arg, long id) {
	this.orig = arg;
	this.slotID = id;

    }

    public long getSlotID() {
	return slotID;
    }

    public String getManufactor() {
	return new String(orig.getManufacturerID(), StandardCharsets.UTF_8).trim();
    }

    public String getSlotDescription() {
	return new String(orig.getSlotDescription(), StandardCharsets.UTF_8).trim();
    }

    public CK_VERSION getHardwareVersion() {
	return orig.getHardwareVersion();
    }

    public CK_VERSION getFirmwareVersion() {
	return orig.getFirmwareVersion();
    }

    public long getFlags() {
	return orig.getFlags().longValue();
    }

}
