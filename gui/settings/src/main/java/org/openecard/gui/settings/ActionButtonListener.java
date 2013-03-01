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

package org.openecard.gui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.plugins.PluginAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple ActionListener performing the action belonging to the pressed action button.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class ActionButtonListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(ActionButtonListener.class);

    private final PluginAction action;

    /**
     * Create a new instance of ActionButtonListener.
     * 
     * @param action The action that will be performed when an ActionEvent occurs.
     */
    ActionButtonListener(PluginAction action) {
	this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	Thread actionThread = new Thread(new Runnable() {
	    @Override
	    public void run() {
		try {
		    action.perform();
		} catch (InvocationTargetException ex) {
		    logger.error(ex.getMessage(), ex);
		} catch (DispatcherException ex) {
		    logger.error(ex.getMessage(), ex);
		}
	    }
	}, "PluginAction");
	actionThread.start();
    }

}
