/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import org.openecard.common.event.EventType;
import org.openecard.common.event.IfdEventObject;
import org.openecard.common.interfaces.EventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Event sink checking for a card removed event of a particular card.
 *
 * @author Hans-Martin Haase
 */
public class CardRemovedFilter implements EventFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CardRemovedFilter.class);

    private final String ifdName;
    private final BigInteger slotIdx;

    public CardRemovedFilter(@Nonnull String ifdName, @Nonnull BigInteger slotIdx) {
	this.ifdName = ifdName;
	this.slotIdx = slotIdx;
    }

    @Override
    public boolean matches(EventType t, Object o) {
	if (t.equals(EventType.CARD_REMOVED)) {
	    LOG.debug("Received CARD_REMOVED event.");
	    ConnectionHandleType conHandle = null;
	    if (o instanceof IfdEventObject) {
		conHandle = ((IfdEventObject) o).getHandle();
	    } else if (o instanceof ConnectionHandleType) {
		conHandle = (ConnectionHandleType) o;
	    }
	    if (conHandle != null && ifdName.equals(conHandle.getIFDName()) && slotIdx.equals(conHandle.getSlotIndex())) {
		LOG.info("Card removed during processing of EAC GUI.");
		return true;
	    } else {
		LOG.debug("An unrelated card has been removed.");
		return false;
	    }
	}

	return false;
    }

}
