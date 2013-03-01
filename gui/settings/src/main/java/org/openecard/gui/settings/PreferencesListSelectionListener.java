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

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openecard.plugins.manager.PluginManager;


/**
 * This Listener updates the middle pane depending on the selection in the preferences list.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
final class PreferencesListSelectionListener implements ListSelectionListener {

    private final SettingsDialog settingsDialog;
    private JPanel mainPanel;
    private JPanel middelListPane;
    private JList listPlugins;
    private JScrollPane listScrollerPlugins;

    PreferencesListSelectionListener(SettingsDialog settingsDialog) {
	this.settingsDialog = settingsDialog;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
	this.mainPanel = settingsDialog.getMainPane();
	this.middelListPane = settingsDialog.getMiddleListPane();

	// clear and remove old middle pane
	mainPanel.remove(middelListPane);
	middelListPane.removeAll();

	// build and add content to middle pane
	//TODO: currently always the plugin content is added no matter what is selected in preferences list
	listScrollerPlugins = createPluginsPane();
	middelListPane.add(listScrollerPlugins);

	//add new middle pane and update
	mainPanel.add(settingsDialog.getMiddleListPane());
	mainPanel.validate();
	mainPanel.repaint();
    }

    /**
     * Create the list containing the plugins wrapped by a JScrollPane.
     * 
     * @return The JScrollPane
     */
    private JScrollPane createPluginsPane() {
	Object[] pluginsArray = PluginManager.getLoadedPlugins().keySet().toArray();
	listPlugins = new JList(pluginsArray);
	listPlugins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	listPlugins.setLayoutOrientation(JList.VERTICAL);
	listPlugins.setVisibleRowCount(-1);
	listPlugins.addListSelectionListener(new PluginsListSelectionListener(settingsDialog, listPlugins));

	listScrollerPlugins = new JScrollPane(listPlugins);
	listScrollerPlugins.setPreferredSize(settingsDialog.getColumnDimension());
	return listScrollerPlugins;
    }

}
