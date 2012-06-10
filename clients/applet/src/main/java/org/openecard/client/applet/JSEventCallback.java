/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.applet;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import netscape.javascript.JSObject;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.util.ByteUtils;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class JSEventCallback implements EventCallback {

    private ECardApplet applet;
    private JSObject jso;

    public JSEventCallback(ECardApplet applet) {
	this.applet = applet;
	jso = JSObject.getWindow(applet);
    }

    @Override
	public void signalEvent(EventType eventType, Object eventData) {
	if (eventData instanceof ConnectionHandleType) {
	    try {
		String args = toJSON(eventType, (ConnectionHandleType) eventData);
		jso.call("signalEvent", new String[]{args});
	    } catch(Exception e) {
		e.printStackTrace(System.out);
	    }
	}
    }

    public void showMessage(String message) {
	jso.call("showMessage", new String[]{message});
    }

    private String toJSON(EventType type, ConnectionHandleType cHandle) {
	String contextHandle = ByteUtils.toHexString(cHandle.getContextHandle());
	String ifdName = cHandle.getIFDName();
	String cardType = cHandle.getRecognitionInfo() != null ? cHandle.getRecognitionInfo().getCardType() : null;
	String eventType = type.name();
	String slotHandle = cHandle.getSlotHandle() != null ? ByteUtils.toHexString(cHandle.getSlotHandle()) : "";

	StringBuilder sb = new StringBuilder();
	sb.append("{");
	sb.append("\"").append("id").append("\"").append(":").append("\"").append(makeId(ifdName)).append("\"").append(",");
	sb.append("\"").append("name").append("\"").append(":").append("\"").append(ifdName).append("\"").append(",");
	sb.append("\"").append("cardType").append("\"").append(":").append("\"").append(cardType).append("\"").append(",");
	sb.append("\"").append("eventType").append("\"").append(":").append("\"").append(eventType).append("\"").append(",");
	sb.append("\"").append("reportId").append("\"").append(":").append("\"").append(applet.getReportID()).append("\"").append(",");
	sb.append("\"").append("contextHandle").append("\"").append(":").append("\"").append(contextHandle).append("\"").append(",");
	sb.append("\"").append("slotHandle").append("\"").append(":").append("\"").append(slotHandle).append("\"");

	sb.append("}");

	return sb.toString();
    }

    private String makeId(String input) {
	try {
	    MessageDigest md = MessageDigest.getInstance("SHA");
	    return ByteUtils.toHexString(md.digest(input.getBytes()));
	} catch (NoSuchAlgorithmException ex) {
	    //FIXME
	    // Das kann so nicht stimmen!
	    return input.replaceAll(" ", "_");
	}
    }

}
