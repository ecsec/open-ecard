/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.addon.manifest.ScalarListEntryType;
import org.openecard.common.I18n;
import org.openecard.crypto.tls.proxy.ProxySettings;
import org.openecard.richclient.gui.components.ScalarListItem;


/**
 * Custom settings group for proxy settings.
 * The settings are made dynamic to reflect the choice made by the user.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class ConnectionSettingsGroup extends OpenecardPropertiesSettingsGroup {

    private static final long serialVersionUID = 1L;
    private static final I18n lang = I18n.getTranslation("addon");
    private static final String GROUP       = "addon.list.core.connection.proxy.group_name";
    private static final String SCHEME      = "addon.list.core.connection.proxy.scheme";
    private static final String	SCHEME_DESC = "addon.list.core.connection.proxy.scheme.desc";
    private static final String	HOST        = "addon.list.core.connection.proxy.host";
    private static final String	HOST_DESC   = "addon.list.core.connection.proxy.host.desc";
    private static final String	PORT        = "addon.list.core.connection.proxy.port";
    private static final String	PORT_DESC   = "addon.list.core.connection.proxy.port.desc";
    private static final String	VALI        = "addon.list.core.connection.proxy.vali";
    private static final String	VALI_DESC   = "addon.list.core.connection.proxy.vali.desc";
    private static final String	USER        = "addon.list.core.connection.proxy.user";
    private static final String	USER_DESC   = "addon.list.core.connection.proxy.user.desc";
    private static final String	PASS        = "addon.list.core.connection.proxy.pass";
    private static final String	PASS_DESC   = "addon.list.core.connection.proxy.pass.desc";
    private static final String	EXCL        = "addon.list.core.connection.proxy.excludes";
    private static final String	EXCL_DESC   = "addon.list.core.connection.proxy.excludes.desc";

    private final JComboBox selection;
    private final JTextField host;
    private final JTextField port;
    private final JCheckBox vali;
    private final JTextField user;
    private final JTextField pass;
    private final ScalarListItem excl;


    public ConnectionSettingsGroup() {
	super(lang.translationForKey(GROUP));

	selection = addSelectionItem(lang.translationForKey(SCHEME), lang.translationForKey(SCHEME_DESC),
		"proxy.scheme", "System Proxy", "SOCKS", "HTTP", "HTTPS", "No Proxy");
	host = addInputItem(lang.translationForKey(HOST), lang.translationForKey(HOST_DESC), "proxy.host");
	port = addInputItem(lang.translationForKey(PORT), lang.translationForKey(PORT_DESC), "proxy.port");
	vali = addBoolItem(lang.translationForKey(VALI), lang.translationForKey(VALI_DESC), "proxy.validate_tls");
	user = addInputItem(lang.translationForKey(USER), lang.translationForKey(USER_DESC), "proxy.user");
	pass = addInputItem(lang.translationForKey(PASS), lang.translationForKey(PASS_DESC), "proxy.pass", true);
	excl = addScalarListItem(lang.translationForKey(EXCL), lang.translationForKey(EXCL_DESC), "proxy.excludes",
		ScalarListEntryType.STRING);

	// register event and trigger initial setup
	ItemManager manager = new ItemManager();
	Object selectedItem = selection.getSelectedItem();
	ItemEvent trigger = new ItemEvent(selection, ItemEvent.ITEM_FIRST, selectedItem, ItemEvent.SELECTED);
	manager.itemStateChanged(trigger);
	selection.addItemListener(manager);
    }

    @Override
    protected void saveProperties() throws IOException, SecurityException, AddonPropertiesException {
	super.saveProperties();
	// reload proxy settings
	ProxySettings.load();
    }


    private class ItemManager implements ItemListener {

	@Override
	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		Object val = e.getItem();
		if ("SOCKS".equals(val)) {
		    setEnabledComponent(host, true);
		    setEnabledComponent(port, true);
		    setEnabledComponent(vali, false);
		    setEnabledComponent(user, false);
		    setEnabledComponent(pass, false);
		    setEnabledComponent(excl, true);
		} else if ("HTTP".equals(val)) {
		    setEnabledComponent(host, true);
		    setEnabledComponent(port, true);
		    setEnabledComponent(vali, false);
		    setEnabledComponent(user, true);
		    setEnabledComponent(pass, true);
		    setEnabledComponent(excl, true);
		} else if ("HTTPS".equals(val)) {
		    setEnabledComponent(host, true);
		    setEnabledComponent(port, true);
		    setEnabledComponent(vali, true);
		    setEnabledComponent(user, true);
		    setEnabledComponent(pass, true);
		    setEnabledComponent(excl, true);
		} else {
		    setEnabledComponent(host, false);
		    setEnabledComponent(port, false);
		    setEnabledComponent(vali, false);
		    setEnabledComponent(user, false);
		    setEnabledComponent(pass, false);
		    setEnabledComponent(excl, false);
		}
	    }
	}

    }

}
