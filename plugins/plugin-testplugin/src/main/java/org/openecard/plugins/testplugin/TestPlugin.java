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

package org.openecard.plugins.testplugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.plugins.PluginAction;
import org.openecard.plugins.PluginInterface;
import org.openecard.plugins.wrapper.PluginDispatcher;
import org.openecard.plugins.wrapper.PluginUserConsent;
import org.openecard.recognition.CardRecognition;


/**
 * Simple Plugin for test purpose.
 * <br/> This plugin provides malicious and good-natured actions.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TestPlugin extends PluginInterface {

    private List<PluginAction> actions = new ArrayList<PluginAction>();

    @Override
    public void initialize(PluginDispatcher dispatcher, PluginUserConsent gui, CardRecognition rec, CardStateMap map) {
	actions.clear();
	actions.add(new GoodAction(dispatcher, gui));
	actions.add(new ReflectionAction(dispatcher));
	actions.add(new ClassLoaderAction());
	actions.add(new ExistingClassAction());
    }

    @Override
    public String getName() {
	return "TestPlugin";
    }

    @Override
    public String getDescription() {
	return "This is a plugin for test purposes";
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
