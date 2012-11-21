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

package org.openecard.control.binding.intent;

import org.openecard.control.binding.ControlBinding;
import org.openecard.control.handler.ControlHandlers;


/**
 * Implements an Intent binding for the control interface.
 *
 * @author Dirk Petrautzki  <petrautzki@hs-coburg.de>
 */
public class IntentBinding extends ControlBinding {

    // The port 24727 according to BSI-TR-03112 is set in the Android client Manifest

    @Override
    public void start() throws Exception {
	// nothing to do here
    }

    public ControlHandlers getHandlers() {
	return handlers;
    }

    @Override
    public void stop() throws Exception {
	// nothing to do here
    }

}
