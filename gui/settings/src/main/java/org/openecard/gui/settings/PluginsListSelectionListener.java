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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openecard.common.I18n;
import org.openecard.gui.swing.common.GUIDefaults;
import org.openecard.plugins.PluginAction;
import org.openecard.plugins.PluginInterface;
import org.openecard.plugins.manager.PluginManager;


/**
 * This Listener updates the right pane depending on the selection in the plugins list.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class PluginsListSelectionListener implements ListSelectionListener {

    private final I18n lang = I18n.getTranslation("settings");

    // translation constants
    private static final String SETTINGS = "settings.plugins.settings";
    private static final String ACTIONS = "settings.plugins.actions";
    private static final String INFORMATION = "settings.plugins.information";
    private static final String DESCRIPTION = "settings.plugins.description";
    private static final String ACTIONS_START = "settings.plugins.actions.start";
    private static final String DEACTIVATED = "settings.plugins.deactivated";
    private static final String ACTIVATED = "settings.plugins.activated";
    private static final String VERSION = "settings.plugins.version";
    private static final String ACTIVATE = "settings.plugins.activate";
    private static final String DEACTIVATE = "settings.plugins.deactivate";
    private static final String CURRENT_STATUS = "settings.plugins.current_status";
    private static final String SETTINGS_NOSETTINGS = "settings.plugins.settings.nosettings";
    private static final String NOACTIONS = "settings.plugins.noactions";

    private static final Icon OPENED_INDICATOR = GUIDefaults.getImage("ToggleText.selectedIcon");
    private static final Icon CLOSED_INDICATOR = GUIDefaults.getImage("ToggleText.icon");

    private final JList listPlugins;
    private final JPanel mainPanel;
    private final JPanel rightPane;
    private final int columnheight;
    private final Dimension dimension;
    private final Border defaultBorder;

    PluginsListSelectionListener(SettingsDialog settingsDialog, JList listPlugins) {
	this.listPlugins = listPlugins;
	rightPane = settingsDialog.getRightPane();
	this.mainPanel = settingsDialog.getMainPane();
	this.defaultBorder = settingsDialog.getDefaultBorder();
	columnheight = settingsDialog.getColumnheight();
	dimension = new Dimension(3 * settingsDialog.getColumnwidth(), settingsDialog.getColumnheight());
    }

    @Override
    public void valueChanged(ListSelectionEvent arg0) {
	Object[] pluginsArray = PluginManager.getLoadedPlugins().keySet().toArray();
	PluginInterface plugin = (PluginInterface) pluginsArray[listPlugins.getSelectedIndex()];

	rightPane.removeAll();
	mainPanel.remove(rightPane);
	JTabbedPane tabbedPane = createTabbedPane(plugin);
	rightPane.add(tabbedPane);
	mainPanel.add(rightPane);
	mainPanel.validate();
	mainPanel.repaint();
    }

    /**
     * Create a TabbedPane containing the information for a plugin.
     * 
     * @param plugin The selected plugin
     * @return The TabbedPane containing the information for the plugin
     */
    private JTabbedPane createTabbedPane(PluginInterface plugin) {
	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	tabbedPane.setBorder(defaultBorder);
	tabbedPane.setPreferredSize(dimension);
	tabbedPane.setBackground(Color.white);
	tabbedPane.setSize(dimension);
	tabbedPane.removeAll();
	tabbedPane.addTab(lang.translationForKey(INFORMATION), createInformationPane(plugin));
	tabbedPane.addTab(lang.translationForKey(ACTIONS), createActionsPane(plugin));
	tabbedPane.addTab(lang.translationForKey(SETTINGS), createSettingsPane(plugin));
	return tabbedPane;
    }

    /**
     * Create the Pane containing the settings for the selected plugin.
     * 
     * @param plugin The selected plugin
     * @return The Pane containing the settings for the selected plugin
     */
    private Component createSettingsPane(PluginInterface plugin) {
	JPanel settingsPane = new JPanel();
	JLabel noSettings = new JLabel();
	noSettings.setText(lang.translationForKey(SETTINGS_NOSETTINGS));
	noSettings.setBorder(defaultBorder);
	settingsPane.add(noSettings);
	return settingsPane;
    }

    /**
     * Create the Pane containing the description for the selected plugin.
     * 
     * @param plugin The selected plugin
     * @return The Pane containing the description for the selected plugin
     */
    private Component createInformationPane(final PluginInterface plugin) {
	final JPanel informationPane = new JPanel();
	informationPane.setLayout(new BoxLayout(informationPane, BoxLayout.PAGE_AXIS));

	// description of the plugin
	JPanel description = new JPanel();
	description.setLayout(new BoxLayout(description, BoxLayout.PAGE_AXIS));
	description.setBorder(defaultBorder);
	JLabel descriptionTitle = new JLabel();
	descriptionTitle.setText(lang.translationForKey(DESCRIPTION));
	description.add(descriptionTitle);
	JLabel descriptionBody = new JLabel();
	descriptionBody.setText(plugin.getDescription());
	description.add(descriptionBody);
	informationPane.add(description);

	// current status of the plugin and status change button
	JLabel statusTitle = new JLabel();
	boolean status = PluginManager.getLoadedPlugins().get(plugin);
	statusTitle.setBorder(defaultBorder);
	String currentStatus = lang.translationForKey(CURRENT_STATUS);
	String activated = lang.translationForKey(ACTIVATED);
	String deactivated = lang.translationForKey(DEACTIVATED);
	statusTitle.setText(currentStatus + (status ? activated : deactivated));
	informationPane.add(statusTitle);
	final JButton statusChangeButton = new JButton();
	statusChangeButton.setText(status ? lang.translationForKey(DEACTIVATE) : lang.translationForKey(ACTIVATE));
	statusChangeButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		boolean status = PluginManager.getLoadedPlugins().get(plugin);
		if (status) {
		    PluginManager.getInstance().deactivatePlugin(plugin);
		    statusChangeButton.setText(lang.translationForKey(ACTIVATE));
		    valueChanged(null);
		} else {
		    PluginManager.getInstance().activatePlugin(plugin);
		    statusChangeButton.setText(lang.translationForKey(DEACTIVATE));
		    valueChanged(null);
		}
	    }
	});
	informationPane.add(statusChangeButton);

	// version of the plugin
	JLabel version = new JLabel();
	version.setBorder(defaultBorder);
	version.setText(lang.translationForKey(VERSION) + plugin.getVersion());
	informationPane.add(version);

	return informationPane;
    }

    /**
     * Create the Pane containing all actions for the selected plugin.
     * 
     * @param plugin The selected plugin
     * @return The Pane containing all actions for the selected plugin
     */
    private Component createActionsPane(PluginInterface plugin) {
	JPanel actionsPanel = new JPanel();
	actionsPanel.setLayout(new GridBagLayout());
	actionsPanel.setMinimumSize(new Dimension(0, columnheight));

	for (int i = 0; i < plugin.getActions().size(); i++) {
	    JPanel actionsPane = createActionPane(plugin.getActions().get(i), actionsPanel);
	    GridBagConstraints c = new GridBagConstraints();
	    c.gridx = 0;
	    c.gridy = i;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weightx = 1;
	    actionsPanel.add(actionsPane, c);
	}

	// add no actions text
	if (plugin.getActions().size() == 0) {
	    JLabel noActions = new JLabel();
	    noActions.setBorder(defaultBorder);
	    noActions.setText(lang.translationForKey(NOACTIONS));
	    actionsPanel.add(noActions);
	}
	return actionsPanel;
    }

    /**
     * Create the Pane for one action of a plugin.
     * 
     * @param plugin The selected plugin
     * @param actionsPanel The panel the created action pane will be added to
     * @param index Index of the action
     */
    private JPanel createActionPane(PluginAction action, JPanel actionsPanel) {
	final JPanel actionPane = new JPanel();
	final JButton button = new JButton(action.getName());
	final JTextArea text = new JTextArea(action.getDescription());
	final JButton startButton = new JButton(lang.translationForKey(ACTIONS_START));

	startButton.addActionListener(new ActionButtonListener(action));

	text.setMargin(new Insets(0, 13, 0, 0));
	text.setEditable(false);
	text.setLineWrap(true);
	text.setWrapStyleWord(true);
	text.setVisible(false);
	startButton.setVisible(false);

	button.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		text.setVisible(!text.isVisible());
		startButton.setVisible(!startButton.isVisible());
		button.setIcon(text.isVisible() ? OPENED_INDICATOR : CLOSED_INDICATOR);
		actionPane.revalidate();
		actionPane.doLayout();
		actionPane.repaint();
	    }

	});

	button.setIcon(text.isVisible() ? OPENED_INDICATOR : CLOSED_INDICATOR);

	button.setOpaque(true);
	button.setFocusPainted(false);
	button.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
	button.setHorizontalAlignment(SwingConstants.LEFT);
	button.setMargin(new Insets(0, 0, 0, 0));
	button.setBounds(0, 0, 0, 0);
	button.setContentAreaFilled(false);
	button.setHorizontalTextPosition(SwingConstants.TRAILING);

	actionPane.setLayout(new GridBagLayout());

	GridBagConstraints gbc = new GridBagConstraints();

	// Add elements
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.weightx = 1.0;
	actionPane.add(button, gbc);

	gbc.gridx = 0;
	gbc.gridy = 1;
	actionPane.add(text, gbc);
	gbc.gridx = 0;
	gbc.fill = GridBagConstraints.NONE;
	gbc.gridy = 2;
	actionPane.add(startButton, gbc);

	return actionPane;
    }

}
