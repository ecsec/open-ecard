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

package org.openecard.ws.marshal;

import java.lang.reflect.InvocationTargetException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


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
