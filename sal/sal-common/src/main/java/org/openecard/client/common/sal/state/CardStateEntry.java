/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard Client.
 *
 * GNU General Public License Usage
 *
 * Open eCard Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open eCard Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Other Usage
 *
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ****************************************************************************/

package org.openecard.client.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.common.sal.state.cif.CardInfoWrapper;
import org.openecard.ws.protocols.tls.v1.TLSMarkerType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CardStateEntry implements Comparable<CardStateEntry> {

    // this number is used as an number authority, so each entry can have a distinct number
    private static int numberRegistry = 0;
    private synchronized static int nextNumber() {
	return numberRegistry++;
    }

    private final int serialNumber;


    private final ConnectionHandleType handle;

    private CardInfoWrapper infoObject;
    private Map<String, Protocol> protoObjects = new TreeMap<String, Protocol>();


    public CardStateEntry(ConnectionHandleType handle) {
	serialNumber = nextNumber();
	this.handle = handle;
    }

    public ConnectionHandleType handleCopy() {
	// TODO: copy handle
	return handle;
    }


    public boolean hasSlotIdx() {
	return handle.getSlotIndex() != null;
    }
    public boolean matchSlotIdx(BigInteger idx) {
	BigInteger otherIdx = handle.getSlotIndex();
	if (otherIdx != null && otherIdx != null && otherIdx.equals(idx)) {
	    return true;
	}
	return false;
    }

    public String getIfdName() {
	return handle.getIFDName();
    }


    public void setInfo (CardInfoType info) {
	infoObject = new CardInfoWrapper(info);
    }
    public void setInfo (TLSMarkerType info) {
	infoObject = new CardInfoWrapper(info);
    }

    public CardInfoWrapper getInfo() {
	return infoObject;
    }


    public Protocol setProtocol(String type, Protocol proto) {
	protoObjects.put(type, proto);
	return proto;
    }

    public Protocol getProtocol(String type) {
	return protoObjects.get(type);
    }

    public void removeProtocol(String type) {
	protoObjects.remove(type);
    }


    ///
    /// needed to sort these entries in sets
    ///

    @Override
    public int compareTo(CardStateEntry o) {
	return this.serialNumber - o.serialNumber;
    }

}
