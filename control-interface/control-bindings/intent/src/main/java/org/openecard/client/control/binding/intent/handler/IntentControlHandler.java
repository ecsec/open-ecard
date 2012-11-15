/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.control.binding.intent.handler;

import android.content.Intent;
import org.openecard.client.control.handler.ControlHandler;


/**
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public abstract class IntentControlHandler extends ControlHandler {

    /**
     * Creates a new IntentControlHandler.
     *
     * @param path Path
     */
    public IntentControlHandler(String path) {
	super(path);
    }

    /**
     * Handles an incoming Intent and creates the corresponding outgoing Intent.
     * @param i incoming Intent containing the request (e.g. for an eID-Client)
     * @return outgoing intent for the browser
     */
    public abstract Intent handle(Intent i);

}
