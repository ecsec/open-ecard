package de.ecsec.ecard.client;

import de.ecsec.core.common.enums.EventType;
import de.ecsec.core.common.interfaces.EventCallback;
import de.ecsec.core.common.util.Helper;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import netscape.javascript.JSObject;


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
        System.out.println("JSEventCallback :: signalEvent(...) " + eventType.name());
        jso.call("showMessage", new String[]{"JSEventCallback :: signalEvent(...) <br> Event: " + eventType.name()});
        
        if (eventData instanceof ConnectionHandleType) {
            String args = toJSON(eventType, (ConnectionHandleType) eventData);
            jso.call("signalEvent", new String[]{args});
        }
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
        sb.append("\"").append("reportId").append("\"").append(":").append("\"").append(applet.getReportId()).append("\"");
        sb.append("}");
        
        return sb.toString();
    }
    
    private String makeId(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA"); 
            return Helper.convByteArrayToString(md.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            return input.replaceAll(" ", "_");
        }
    }
}
