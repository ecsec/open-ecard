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

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.datatype.XMLGregorianCalendar;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.common.sal.state.cif.CardInfoWrapper;
import org.openecard.client.common.util.ByteUtils;


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

    private final CardInfoWrapper infoObject;
    private Map<String, Protocol> protoObjects = new TreeMap<String, Protocol>();


    public CardStateEntry(ConnectionHandleType handle, CardInfoType cif) {
	serialNumber = nextNumber();
	infoObject = new CardInfoWrapper(cif);
	this.handle = handle;
    }


    public ConnectionHandleType handleCopy() {
	ConnectionHandleType result = new ConnectionHandleType();
	copyPath(result, handle);
	result.setSlotHandle(ByteUtils.clone(handle.getSlotHandle()));
	result.setRecognitionInfo(copyRecognition(handle.getRecognitionInfo()));
	return result;
    }
    public CardApplicationPathType pathCopy() {
	CardApplicationPathType result = new CardApplicationPathType();
	copyPath(result, handle);
	return result;
    }

    private static void copyPath(CardApplicationPathType out, CardApplicationPathType in) {
	out.setCardApplication(ByteUtils.clone(in.getCardApplication()));
	out.setChannelHandle(copyChannel(in.getChannelHandle()));
	out.setContextHandle(ByteUtils.clone(in.getContextHandle()));
	out.setIFDName(in.getIFDName());
	out.setSlotIndex(in.getSlotIndex()); // TODO: copy bigint
    }
    private static ChannelHandleType copyChannel(ChannelHandleType handle) {
	if (handle == null) {
	    return null;
	}
	ChannelHandleType result = new ChannelHandleType();
	result.setBinding(handle.getBinding());
	result.setPathSecurity(copyPathSec(handle.getPathSecurity()));
	result.setProtocolTerminationPoint(handle.getProtocolTerminationPoint());
	result.setSessionIdentifier(handle.getSessionIdentifier());
	return result;
    }
    private static ConnectionHandleType.RecognitionInfo copyRecognition(ConnectionHandleType.RecognitionInfo rec) {
	if (rec == null) {
	    return null;
	}
	ConnectionHandleType.RecognitionInfo result = new ConnectionHandleType.RecognitionInfo();
	if (rec.getCaptureTime() != null) {
	    result.setCaptureTime((XMLGregorianCalendar)rec.getCaptureTime().clone());
	}
	result.setCardIdentifier(ByteUtils.clone(rec.getCardIdentifier()));
	result.setCardType(rec.getCardType());
	return result;
    }
    private static PathSecurityType copyPathSec(PathSecurityType sec) {
	if (sec == null) {
	    return null;
	}
	PathSecurityType result = new PathSecurityType();
	result.setParameters(sec.getParameters()); // TODO: copy depending on actual content
	result.setProtocol(sec.getProtocol());
	return result;
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
