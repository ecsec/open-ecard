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
import java.util.List;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.plugins.wrapper.PluginDispatcher;
import org.openecard.plugins.wrapper.PluginUserConsent;
import org.openecard.recognition.CardRecognition;


/**
 * Base interface every plugin must implement.
 * This interface defines the methods that plugins must implement.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public abstract class PluginInterface {

    /**
     * Get the localized name of the plugin.
     * 
     * @return The localized name of the plugin.
     */
    public abstract String getName();

    /**
     * Get the localized description of the plugin.
     * 
     * @return The localized description of the plugin.
     */
    public abstract String getDescription();

    /**
     * Get the stream for the logo of this plugin or null if no logo exists.
     * 
     * @return Logo for this plugin or null if no logo exists
     */
    public abstract InputStream getLogo();

    /**
     * Get the version of the plugin.
     * 
     * @return The version of the plugin.
     */
    public abstract String getVersion();

    /**
     * Get a list of actions the plugin provides.
     * 
     * @return A list of Actions the plugin provides.
     */
    public abstract List<PluginAction> getActions();

    /**
     * Initialize and start the plugin's services.
     * 
     * @param dispatcher PluginDispatcher wrapper the dispatcher to use
     * @param gui PluginUserConsent wrapping the UserConsent to use
     * @param rec CardRecognition to use
     * @param map CardStateMap of the client
     */
    public abstract void initialize(PluginDispatcher dispatcher, PluginUserConsent gui, CardRecognition rec, CardStateMap map);

    /**
     * Stop any services the plugin is running.
     */
    public abstract void stop();

    @Override
    public String toString() {
	return this.getName();
    }

}
