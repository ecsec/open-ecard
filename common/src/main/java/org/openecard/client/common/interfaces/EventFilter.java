package org.openecard.client.common.interfaces;

import org.openecard.client.common.enums.EventType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface EventFilter {

    public boolean matches(EventType t, Object o);

}
