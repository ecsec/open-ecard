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

package org.openecard.client.control;

import org.openecard.client.control.binding.ControlBinding;
import org.openecard.client.control.handler.ControlHandlers;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ControlInterface {

    private final ControlBinding binding;

    /**
     * Creates a new ControlInterface.
     * 
     * @param binding ControlBinding
     */
    public ControlInterface(ControlBinding binding) {
	this(binding, new ControlHandlers());
    }

    /**
     * Creates a new ControlInterface.
     * 
     * @param binding ControlBinding
     * @param handlers ControlHandlers
     */
    public ControlInterface(ControlBinding binding, ControlHandlers handlers) {
	this.binding = binding;
	this.binding.setControlHandlers(handlers);
    }

    /**
     * Starts the control interface.
     */
    public void start() throws Exception {
	binding.start();
    }

    /**
     * Stops the control interface.
     */
    public void stop() throws Exception {
	binding.stop();
    }

}
