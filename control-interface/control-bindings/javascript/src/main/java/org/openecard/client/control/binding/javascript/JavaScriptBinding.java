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

package org.openecard.client.control.binding.javascript;

import org.openecard.client.control.binding.ControlBinding;
import org.openecard.client.control.binding.javascript.handler.StatusHandler;
import org.openecard.client.control.binding.javascript.handler.TCTokenHandler;
import org.openecard.client.control.handler.ControlHandlers;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class JavaScriptBinding extends ControlBinding {

    private JavaScriptService service;

    /**
     * Creates a new JavaScriptBinding.
     */
    public JavaScriptBinding() throws Exception {
    }

    public Object[] handle(String id, Object[] data) {
	return service.handle(id, data);
    }
    
    @Override
    public void start() throws Exception {
	// Add default handlers if none are given
	if (handlers == null || handlers.getControlHandlers().isEmpty()) {
	    handlers = new ControlHandlers();
	    handlers.addControlHandler(new TCTokenHandler(listeners));
	    handlers.addControlHandler(new StatusHandler(listeners));
	}

	service = new JavaScriptService(handlers);
	service.start();
    }

    @Override
    public void stop() throws Exception {
	service.interrupt();
    }

}
