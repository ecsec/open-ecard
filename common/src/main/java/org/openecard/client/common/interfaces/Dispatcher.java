package org.openecard.client.common.interfaces;

import java.lang.reflect.InvocationTargetException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface Dispatcher {

    public Object deliver(Object request) throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException;

}
