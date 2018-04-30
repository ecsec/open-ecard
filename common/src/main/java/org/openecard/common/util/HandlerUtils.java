/****************************************************************************
 * Copyright (C) 2014-2016 ecsec GmbH.
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
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for {@code CardApplicationPathType} and {@code ConnectionHandleType}.
 *
 * @author Tobias Wich
 */
public class HandlerUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HandlerUtils.class);

    public HandlerBuilder createBuilder() {
	return HandlerBuilder.create();
    }

    // TODO: use builder to copy handles

    public static ConnectionHandleType copyHandle(ConnectionHandleType handle) {
	ConnectionHandleType result = new ConnectionHandleType();
	copyPath(result, handle);
	result.setSlotHandle(ByteUtils.clone(handle.getSlotHandle()));
	result.setRecognitionInfo(copyRecognition(handle.getRecognitionInfo()));
	result.setSlotInfo(copySlotInfo(handle.getSlotInfo()));
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

    private static ConnectionHandleType.SlotInfo copySlotInfo(ConnectionHandleType.SlotInfo slotInfo) {
	if (slotInfo == null) {
	    return null;
	}

	ConnectionHandleType.SlotInfo result = new ConnectionHandleType.SlotInfo();
	result.setProtectedAuthPath(slotInfo.isProtectedAuthPath());
	return result;
    }


    public static ConnectionHandleType extractHandle(@Nonnull Object obj) {
	// SAL calls
	ConnectionHandleType handle = getMember(obj, "getConnectionHandle", ConnectionHandleType.class);
	if (handle != null) {
	    LOG.debug("Found ConnectionHandle in object of type {}.", obj.getClass().getSimpleName());
	    return handle;
	}

	// IFD calls with context handle
	byte[] ctxHandle = getMember(obj, "getContextHandle", byte[].class);
	if (ctxHandle != null) {
	    LOG.debug("Found ContextHandle in object of type {}.", obj.getClass().getSimpleName());
	    String ifdName = getMember(obj, "getIFDName", String.class);
	    String sessionId = getMember(obj, "getSessionIdentifier", String.class);
	    return HandlerBuilder.create()
		    .setContextHandle(ctxHandle)
		    .setIfdName(ifdName)
		    .setSessionId(sessionId)
		    .buildConnectionHandle();
	}

	// IFD calls with slot handle
	byte[] slotHandle = getMember(obj, "getSlotHandle", byte[].class);
	if (slotHandle != null) {
	    LOG.debug("Found SlotHandle in object of type {}.", obj.getClass().getSimpleName());
	    return HandlerBuilder.create()
		    .setSlotHandle(slotHandle)
		    .buildConnectionHandle();
	}

	// no handle could be determined
	return null;
    }

    private static <T> T getMember(Object obj, String methodName, Class<T> memberType) {
	try {
	    Method getter = obj.getClass().getMethod(methodName);
	    if (memberType.equals(getter.getReturnType())) {
		return memberType.cast(getter.invoke(obj));
	    }
	} catch (ReflectiveOperationException ex) {
	    // nothing found
	}
	return null;
    }

}
