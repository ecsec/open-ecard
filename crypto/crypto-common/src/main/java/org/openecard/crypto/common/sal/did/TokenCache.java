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

package org.openecard.crypto.common.sal.did;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.ByteComparator;


/**
 * Simple cache for the DIDInfos entry point to card data.
 *
 * @author Tobias Wich
 */
public class TokenCache {

    private final Dispatcher dispatcher;
    private final Map<byte[], DidInfos> cachedInfos;

    public TokenCache(@Nonnull Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
	this.cachedInfos = new TreeMap<>(new ByteComparator());
    }

    public DidInfos getInfo(@Nullable char[] pin, @Nonnull ConnectionHandleType handle) {
	return getInfo(pin, handle.getSlotHandle());
    }

    public DidInfos getInfo(@Nullable char[] pin, @Nonnull byte[] slotHandle) {
	DidInfos result = cachedInfos.get(slotHandle);

	if (result == null) {
	    result = new DidInfos(dispatcher, pin, slotHandle);
	}

	return result;
    }

    public void clearPins() {
	Iterator<Map.Entry<byte[], DidInfos>> it = cachedInfos.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<byte[], DidInfos> next = it.next();
	    byte[] slotHandle = next.getKey();
	    DidInfos dids = next.getValue();
	    dids.clearPin(slotHandle);
	}
    }

}
