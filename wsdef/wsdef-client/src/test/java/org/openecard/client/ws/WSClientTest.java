package org.openecard.client.ws;

import org.openecard.client.ws.WSClassLoader;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;
import static junit.framework.Assert.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WSClientTest {

    @Test
    public void instanceTest() throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
	for (String serviceName : WSClassLoader.getSupportedServices()) {
	    Object service = WSClassLoader.getClientService(serviceName);
	    assertNotNull(service);
	}
    }

}
