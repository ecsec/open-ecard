/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.gui.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.swing.common.GUIDefaults;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ToggleText implements StepComponent {

    private final static String TOGGLETEXT = "ToggleText";
    private final static String TOGGLETEXT_FOREGROUND = TOGGLETEXT + ".foreground";
    private final static String TOGGLETEXT_BACKGROUND = TOGGLETEXT + ".background";
    private final static String TOGGLETEXT_FONT = TOGGLETEXT + ".font";
    private final static String TOGGLETEXT_INDICATOR_FOREGROUND = TOGGLETEXT + "Indicator.foreground";
//    private String openedIndicator = " ▼";
    private final static Icon openedIndicator = GUIDefaults.getImage("ToggleText.selectedIcon");
//    private String closedIndicator = " ▲";
    private final static Icon closedIndicator = GUIDefaults.getImage("ToggleText.icon");

    private JPanel rootPanel;
    private JButton button;
    private JTextArea text;

    /**
     * Creates a new ToggleText.
     *
     * @param toggleText
     */
    public ToggleText(org.openecard.gui.definition.ToggleText toggleText) {
	this(toggleText.getTitle(), toggleText.getText(), toggleText.isCollapsed());
    }

    /**
     * Creates a new ToggleText.
     *
     * @param buttonText Text of the button
     * @param contentText Text of the content
     */
    public ToggleText(String buttonText, String contentText) {
	this(buttonText, contentText, false);
    }

    /**
     * Creates a new ToggleText.
     *
     * @param buttonText Text of the button
     * @param contentText Text of the content
     * @param collapsed Collapsed (content is visible or not)
     */
    public ToggleText(String buttonText, String contentText, boolean collapsed) {
	initComponents(buttonText, contentText);
	initLayout();
	loadUIDefaults();

	button.setSelected(collapsed);
	text.setVisible(!collapsed);
	button.setIcon(!collapsed ? openedIndicator : closedIndicator);
    }

    /**
     * Initializes the components of the panel.
     *
     * @param buttonText Text of the button
     * @param contentText Text of the content
     */
    private void initComponents(String buttonText, String contentText) {
	rootPanel = new JPanel();
	button = new JButton(buttonText + "  ");
	text = new JTextArea(contentText);

	button.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		text.setVisible(!text.isVisible());
		button.setIcon(text.isVisible() ? openedIndicator : closedIndicator);
		rootPanel.revalidate();
		rootPanel.doLayout();
		rootPanel.repaint();
	    }
	});
    }

    /**
     * Initializes the layout of the panel.
     */
    private void initLayout() {
	rootPanel.setLayout(new GridBagLayout());

	GridBagConstraints gbc = new GridBagConstraints();

	// Add elements
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.weightx = 1.0;
	rootPanel.add(button, gbc);

	gbc.gridx = 0;
	gbc.gridy = 1;
	rootPanel.add(text, gbc);
    }

    private void loadUIDefaults() {
	UIDefaults defaults = UIManager.getDefaults();

	Color bg = (Color) defaults.get(TOGGLETEXT_BACKGROUND);
	if (bg == null) {
	    bg = Color.WHITE;
	}
	Color fg = (Color) defaults.get(TOGGLETEXT_FOREGROUND);
	if (fg == null) {
	    fg = Color.BLACK;
	}
	Color fgIndicator = (Color) defaults.get(TOGGLETEXT_INDICATOR_FOREGROUND);
	if (fgIndicator == null) {
	    fgIndicator = Color.LIGHT_GRAY;
	}
	Font font = (Font) defaults.get(TOGGLETEXT_FONT);
	if (font == null) {
	    font = button.getFont();
	}

	button.setOpaque(true);
	button.setFocusPainted(false);
	button.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
	button.setHorizontalAlignment(SwingConstants.LEFT);
	button.setMargin(new Insets(0, 0, 0, 0));
	button.setBounds(0, 0, 0, 0);
	button.setFont(font.deriveFont(Font.BOLD));
	button.setContentAreaFilled(false);
	button.setHorizontalTextPosition(SwingConstants.LEADING);

	text.setMargin(new Insets(0, 13, 0, 0));
	text.setEditable(false);
	text.setLineWrap(true);
	text.setWrapStyleWord(true);
	text.setFont(font);

	rootPanel.setBackground(bg);
	rootPanel.setForeground(fg);

	for (int i = 0; i < rootPanel.getComponentCount(); i++) {
	    rootPanel.getComponent(i).setBackground(bg);
	    rootPanel.getComponent(i).setForeground(fg);
	}
    }

    @Override
    public Component getComponent() {
	return rootPanel;
    }

    @Override
    public boolean isValueType() {
	return false;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public OutputInfoUnit getValue() {
	return null;
    }

}
