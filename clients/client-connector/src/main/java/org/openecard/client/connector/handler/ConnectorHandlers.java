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

package org.openecard.client.connector.handler;

import java.util.ArrayList;
import java.util.List;


/**
 * Implements a list of ConnectorHandlers.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ConnectorHandlers {

    private volatile List<ConnectorHandler> connectorHandlers = new ArrayList<ConnectorHandler>();

    /**
     * Creates a new list of ConnectorHandlers.
     *
     * @return List of ConnectorHandlers.
     */
    public List<ConnectorHandler> getConnectorHandlers() {
	return connectorHandlers;
    }

    /**
     * Adds a ConnectorHandler.
     *
     * @param handler Handler.
     */
    public void addConnectorHandler(ConnectorHandler handler) {
	connectorHandlers.add(handler);
    }

    /**
     * Removes the ConnectorHandler.
     *
     * @param handler Handler
     */
    public void removeConnectorHandler(ConnectorHandler handler) {
	connectorHandlers.remove(handler);
    }

}
