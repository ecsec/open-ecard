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

package org.openecard.client.control.client;

import java.util.ArrayList;
import java.util.List;


/**
 * Implements a list of ControlListeners.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ControlListeners {

    private volatile List<ControlListener> controlListeners = new ArrayList<ControlListener>();

    /**
     * Creates a new list of ControlListeners.
     *
     * @return List of ControlListeners
     */
    public List<ControlListener> getControlListeners() {
	return controlListeners;
    }

    /**
     * Adds a ControlListener.
     *
     * @param listener Listener
     */
    public void addControlListener(ControlListener listener) {
	controlListeners.add(listener);
    }

    /**
     * Removes the ControlListener.
     *
     * @param listener Listener
     */
    public void removeControlListener(ControlListener listener) {
	controlListeners.remove(listener);
    }

}
