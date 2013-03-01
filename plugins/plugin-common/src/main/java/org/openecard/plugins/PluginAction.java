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

package org.openecard.plugins;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import org.openecard.common.interfaces.DispatcherException;


/**
 * Base interface every action must implement.
 * This interface defines the methods that PluginActions must implement.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public interface PluginAction {

    /**
     * Get ID of the action.
     *
     * @return The ID of this action.
     */
    String getID();

    /**
     * Get the localized name of the action.
     * 
     * @return The localized name of the action.
     */
    String getName();

    /**
     * Get the localized description of the action.
     * 
     * @return The localized description of the action.
     */
    String getDescription();

    /**
     * Get the stream for the logo of the action or null if no logo exists.
     * 
     * @return Logo for this action or null if no logo exists
     */
    InputStream getLogo();

    /**
     * Perform the action.
     * 
     * @throws InvocationTargetException In case the dispatched method throws an exception.
     * @throws DispatcherException In case a reflection error in the dispatcher occurs.
     */
    void perform() throws DispatcherException, InvocationTargetException;

    /**
     * Get if the action is a main activity.
     * 
     * @return true if this action is set to be a main activity, else false
     */
    boolean isMainActivity();

}
