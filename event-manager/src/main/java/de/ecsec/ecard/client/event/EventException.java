package de.ecsec.ecard.client.event;

import de.ecsec.core.common.ECardException;
import oasis.names.tc.dss._1_0.core.schema.Result;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class EventException extends ECardException {

    private EventException() {}

    public EventException(String message) {
	makeException(this, message);
    }

    public EventException(String message, Throwable cause) {
	makeException(this, cause, message);
    }

    public EventException(Throwable cause) {
	makeException(this, cause);
    }

    public EventException(Result r) {
	makeException(this, r);
    }

}
