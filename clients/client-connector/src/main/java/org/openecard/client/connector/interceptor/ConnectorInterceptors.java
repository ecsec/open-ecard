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

package org.openecard.client.connector.interceptor;

import java.util.ArrayList;
import java.util.List;


/**
 * Implements a list of ConnectorInterceptors.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ConnectorInterceptors {

    private volatile List<ConnectorRequestInterceptor> connectorRequestInterceptors = new ArrayList<ConnectorRequestInterceptor>();
    private volatile List<ConnectorResponseInterceptor> connectorResponseInterceptors = new ArrayList<ConnectorResponseInterceptor>();

    /**
     * Creates a new list of ConnectorInterceptors.
     *
     * @return List of ConnectorInterceptors.
     */
    public ArrayList<ConnectorInterceptor> getConnectorInterceptors() {
	return new ArrayList<ConnectorInterceptor>() {
	    {
		addAll(connectorRequestInterceptors);
		addAll(connectorResponseInterceptors);
	    }

	};
    }

    /**
     * Returns the list of ConnectorRequestInterceptors.
     *
     * @return List of ConnectorRequestInterceptors
     */
    public ArrayList<ConnectorRequestInterceptor> getConnectorRequestInterceptors() {
	return new ArrayList<ConnectorRequestInterceptor>() {
	    {
		addAll(connectorRequestInterceptors);
	    }

	};
    }

    /**
     * Returns the list of ConnectorResponseInterceptors.
     *
     * @return List of ConnectorResponseInterceptors
     */
    public ArrayList<ConnectorResponseInterceptor> getConnectorResponseInterceptors() {
	return new ArrayList<ConnectorResponseInterceptor>() {
	    {
		addAll(connectorResponseInterceptors);
	    }

	};
    }

    /**
     * Adds a ConnectorInterceptor.
     *
     * @param interceptor Interceptor
     */
    public void addConnectorInterceptor(ConnectorInterceptor interceptor) {
	if (interceptor instanceof ConnectorRequestInterceptor) {
	    connectorRequestInterceptors.add((ConnectorRequestInterceptor) interceptor);
	} else if (interceptor instanceof ConnectorResponseInterceptor) {
	    connectorResponseInterceptors.add((ConnectorResponseInterceptor) interceptor);
	} else {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Removes the ConnectorInterceptor.
     *
     * @param interceptor Interceptor
     */
    public void removeConnectorInterceptor(ConnectorInterceptor interceptor) {
	if (interceptor instanceof ConnectorRequestInterceptor) {
	    connectorRequestInterceptors.remove((ConnectorRequestInterceptor) interceptor);
	} else if (interceptor instanceof ConnectorResponseInterceptor) {
	    connectorResponseInterceptors.remove((ConnectorResponseInterceptor) interceptor);
	} else {
	    throw new IllegalArgumentException();
	}
    }

}
