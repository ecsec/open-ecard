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

package org.openecard.plugins.pinplugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.I18n;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.plugins.PluginAction;
import org.openecard.plugins.PluginInterface;
import org.openecard.plugins.wrapper.PluginDispatcher;
import org.openecard.plugins.wrapper.PluginUserConsent;
import org.openecard.recognition.CardRecognition;


/**
 * Plugin for PIN management.
 * This plugin provides two actions for the PIN management. One to change the PIN and one to unblock it.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PINPlugin extends PluginInterface {

    private final I18n lang = I18n.getTranslation("pinplugin");

    private List<PluginAction> actions = new ArrayList<PluginAction>();

    @Override
    public void initialize(PluginDispatcher dispatcher, PluginUserConsent gui, CardRecognition rec, CardStateMap map) {
	actions.clear();
	actions.add(new ChangePINAction(dispatcher, gui, rec, map));
	actions.add(new UnblockPINAction(dispatcher, gui, rec, map));
    }

    @Override
    public String getName() {
	return lang.translationForKey("name");
    }

    @Override
    public String getDescription() {
	return lang.translationForKey("description");
    }

    @Override
    public InputStream getLogo() {
	return null;
    }

    @Override
    public String getVersion() {
	return "1.0";
    }

    @Override
    public List<PluginAction> getActions() {
	return actions;
    }

    @Override
    public void stop() {
	// nothing to do here
    }

}
