/*
 * Copyright 2012 Johannes Schmoelz ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            String args = toJSON(eventType, (ConnectionHandleType) eventData);
            jso.call("signalEvent", new String[]{args});
        }
    }

    public void showMessage(String message) {
        jso.call("showMessage", new String[]{message});
    }

    private String toJSON(EventType type, ConnectionHandleType cHandle) {
        String ifdName = cHandle.getIFDName();
        String cardType = cHandle.getRecognitionInfo() != null ? cHandle.getRecognitionInfo().getCardType() : null;
        String eventType = type.name();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"").append("id").append("\"").append(":").append("\"").append(makeId(ifdName)).append("\"").append(",");
        sb.append("\"").append("name").append("\"").append(":").append("\"").append(ifdName).append("\"").append(",");
        sb.append("\"").append("cardType").append("\"").append(":").append("\"").append(cardType).append("\"").append(",");
        sb.append("\"").append("eventType").append("\"").append(":").append("\"").append(eventType).append("\"").append(",");
        sb.append("\"").append("reportId").append("\"").append(":").append("\"").append(applet.getReportID()).append("\"");
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
