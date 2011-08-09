package de.ecsec.ecard.client.event;

import de.ecsec.core.common.ECardConstants;
import oasis.names.tc.dss._1_0.core.schema.Result;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class EventException extends Exception {
    
    private String resultMajor;
    private String resultMinor;
    
    public EventException() {}
    
    public EventException(String message) {
        super(message);
    }
    
    public EventException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EventException(Throwable cause) {
        super(cause);
    }
    
    public EventException(Result r) {
        super(r.getResultMessage() != null ? r.getResultMessage().getValue() : "Unknown IFD exception occurred.");
        resultMajor = r.getResultMajor() != null ? r.getResultMajor() : ECardConstants.Major.ERROR;
        resultMinor = r.getResultMinor() != null ? r.getResultMinor() : ECardConstants.Minor.App.UNKNOWN_ERROR;
    }
    
    public String getResultMajor() {
        if (resultMajor == null) {
            resultMajor = new String();
        }
        return resultMajor;
    }
    
    public String getResultMinor() {
        if (resultMinor == null) {
            resultMinor = new String();
        }
        return resultMinor;
    }
    
    public String getResultMessage() {
        return getMessage();
    }
}
