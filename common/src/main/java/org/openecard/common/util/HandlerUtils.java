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

package org.openecard.common.util;

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Utility class for {@code CardApplicationPathType} and {@code ConnectionHandleType}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class HandlerUtils {

    public HandlerBuilder createBuilder() {
	return HandlerBuilder.create();
    }

    // TODO: use builder to copy handles

    public static ConnectionHandleType copyHandle(ConnectionHandleType handle) {
	ConnectionHandleType result = new ConnectionHandleType();
	copyPath(result, handle);
	result.setSlotHandle(ByteUtils.clone(handle.getSlotHandle()));
	result.setRecognitionInfo(copyRecognition(handle.getRecognitionInfo()));
	return result;
    }

    public static CardApplicationPathType copyPath(CardApplicationPathType handle) {
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
	    result.setCaptureTime((XMLGregorianCalendar) rec.getCaptureTime().clone());
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

}
