/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.richclient.gui.manage.core;

import java.util.Map;
import org.openecard.common.I18n;
import org.openecard.plugins.PluginAction;
import org.openecard.plugins.PluginInterface;
import org.openecard.plugins.manager.PluginManager;
import org.openecard.richclient.gui.manage.ActionEntryPanel;
import org.openecard.richclient.gui.manage.ActionPanel;


/**
 * Custom action panel with PIN management actions.
 * The actions are extracted from the old plugin mechanism.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PINActionPanel extends ActionPanel {

    private static final long serialVersionUID = 1L;

    private static final String CHANGE = "addon.list.core.pin_management.change_pin";
    private static final String CHANGE_DESC = "addon.list.core.pin_management.change_pin.desc";
    private static final String UNBLOCK = "addon.list.core.pin_management.unblock_pin";
    private static final String UNBLOCK_DESC = "addon.list.core.pin_management.unblock_pin.desc";

    private final I18n lang = I18n.getTranslation("addon");

    public PINActionPanel() {
	PluginAction changePinAction = null;
	PluginAction unblockPinAction = null;
	Map<PluginInterface, Boolean> plugins = PluginManager.getLoadedPlugins();
	for (PluginInterface plugin : plugins.keySet()) {
	    for (PluginAction action : plugin.getActions()) {
		if ("ChangePINAction".equals(action.getID())) {
		    changePinAction = action;
		} else if ("UnblockPINAction".equals(action.getID())) {
		    unblockPinAction = action;
		}
	    }
	}

	String changePinName = lang.translationForKey(CHANGE);
	String changePinDesc = lang.translationForKey(CHANGE_DESC);
	ActionEntryPanel changePin = new ActionEntryPanel(changePinName, changePinDesc);
	changePin.addAction(changePinAction);
	addActionEntry(changePin);

	String unblockPinName = lang.translationForKey(UNBLOCK);
	String unblockPinDesc = lang.translationForKey(UNBLOCK_DESC);
	ActionEntryPanel unblockPin = new ActionEntryPanel(unblockPinName, unblockPinDesc);
	unblockPin.addAction(unblockPinAction);
	addActionEntry(unblockPin);
    }

}
