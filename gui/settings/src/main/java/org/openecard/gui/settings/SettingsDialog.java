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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.openecard.common.I18n;
import org.openecard.gui.graphics.GraphicsUtil;
import org.openecard.gui.graphics.OecLogoBgWhite;


/**
 * This class is used to create a Settings dialog. It shows the current settings of the application, e.g. the loaded
 * plugins, and gives the ability to change them.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SettingsDialog extends JDialog {

    private final I18n lang = I18n.getTranslation("settings");

    private static final long serialVersionUID = 1L;

    private final int width = 720;
    private final int height = 480;
    private final int columnwidth = width / 5;
    private final int columnheight = 380;
    private final Dimension columnDimension = new Dimension(columnwidth, columnheight);

    private final Border defaultBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    private JPanel middleListPane;
    private JPanel rightPane;
    private JPanel listPanePreferences;
    private JPanel mainPane;

    /**
     * Constructor of SettingsDialog class.
     */
    public SettingsDialog() {
	super();
	setupUI();
    }

    public JPanel getRightPane() {
	return rightPane;
    }

    public JPanel getMiddleListPane() {
	return middleListPane;
    }

    public Border getDefaultBorder() {
	return defaultBorder;
    }

    public int getColumnwidth() {
	return columnwidth;
    }

    public int getColumnheight() {
	return columnheight;
    }

    public Dimension getColumnDimension() {
	return columnDimension;
    }

    public JPanel getMainPane() {
	return mainPane;
    }

    /**
     * Convenience method for showing a Settings dialog. Since this method is static, there is no need to create an
     * instance of SettingsDialog to call it.
     */
    public static void showDialog() {
	SettingsDialog dialog = new SettingsDialog();
	dialog.setVisible(true);
    }

    private void setupUI() {
	Image logo = GraphicsUtil.createImage(OecLogoBgWhite.class, 147, 147);

	setSize(width, height);
	setIconImage(logo);
	setTitle(lang.translationForKey("settings.title"));
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	setResizable(false);
	setLocationRelativeTo(null);
	getContentPane().setBackground(null);
	getContentPane().setLayout(new GridBagLayout());

	middleListPane = createMiddleListPane();
	listPanePreferences = createPreferencesListPane();
	mainPane = createMainPane();
	rightPane = createRightPane();

	// Create and layout the main panel
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 1;
	gbc.gridx = 0;
	gbc.gridy = 0;
	getContentPane().add(mainPane, gbc);

	// Create and layout the close button to bottom right.
	JPanel buttonPane = createButtonPane();
	gbc.gridy = 1;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	getContentPane().add(buttonPane, gbc);

    }

    private JPanel createRightPane() {
	JPanel p = new JPanel();
	p.setBackground(null);
	p.setBorder(new EmptyBorder(0, 0, 0, 0));
	return p;
    }

    /**
     * 
     * @return
     */
    private JPanel createMainPane() {
	JPanel p = new JPanel();
	p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
	p.setBackground(null);
	p.add(listPanePreferences);
	return p;
    }

    private JPanel createPreferencesListPane() {
	JList listPreferences = new JList(new Object[] { "Plugins" });
	listPreferences.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	listPreferences.setLayoutOrientation(JList.VERTICAL);
	listPreferences.setVisibleRowCount(-1);

	JScrollPane listScrollerPreferences = new JScrollPane(listPreferences);
	listScrollerPreferences.setPreferredSize(columnDimension);
	listPreferences.addListSelectionListener(new PreferencesListSelectionListener(this));

	JPanel preferences = new JPanel();
	preferences.setLayout(new BoxLayout(preferences, BoxLayout.PAGE_AXIS));
	preferences.add(listScrollerPreferences);
	preferences.setBorder(defaultBorder);
	preferences.setMaximumSize(new Dimension(columnwidth, Integer.MAX_VALUE));
	preferences.setBackground(null);
	return preferences;
    }

    private JPanel createMiddleListPane() {
	JPanel p = new JPanel();
	p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
	p.setMaximumSize(new Dimension(columnwidth, Integer.MAX_VALUE));
	p.setBorder(defaultBorder);
	p.setBackground(null);
	return p;
    }

    /**
     * Create the pane including the close button in the right.
     * 
     * @return The created button pane
     */
    private JPanel createButtonPane() {
	JButton btnClose = new JButton(lang.translationForKey("settings.button.close"));

	btnClose.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispose();
	    }
	});

	JPanel buttonPane = new JPanel();
	buttonPane.setBackground(null);
	buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
	buttonPane.setBorder(defaultBorder);
	buttonPane.add(Box.createHorizontalGlue());
	buttonPane.add(btnClose);
	return buttonPane;
    }

}
