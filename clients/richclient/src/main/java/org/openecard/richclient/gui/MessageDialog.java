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

package org.openecard.richclient.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.openecard.gui.swing.Logo;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class MessageDialog extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextArea messageLabel;
    private JLabel headlineLabel;

    public MessageDialog() {
	this("", "");
    }

    public MessageDialog(String headline, String message) {
	headlineLabel = new JLabel(headline);
	messageLabel = new JTextArea(message);

	setPreferredSize(new Dimension(425, 85));
	initComponents();
    }

    private void initComponents() {
	// Config GUI components
	headlineLabel.setFont(headlineLabel.getFont().deriveFont(Font.BOLD));

	messageLabel.setMargin(new Insets(0, 0, 0, 0));
	messageLabel.setEditable(false);
	messageLabel.setLineWrap(true);
	messageLabel.setWrapStyleWord(true);
	messageLabel.setFont(new JButton().getFont());

	JScrollPane scrollPane = new JScrollPane(messageLabel);
	scrollPane.setBorder(BorderFactory.createEmptyBorder());

	Logo logo = new Logo();

	// Config layout
	GroupLayout layout = new GroupLayout(this);
	setLayout(layout);

	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);

	layout.setHorizontalGroup(
		layout.createSequentialGroup()
		.addComponent(logo, 60, 60, 60)
		.addGap(20)
		.addGroup(layout.createParallelGroup()
		.addComponent(headlineLabel)
		.addComponent(scrollPane)));
	layout.setVerticalGroup(
		layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		.addComponent(logo)
		.addGroup(layout.createSequentialGroup()
		.addComponent(headlineLabel)
		.addComponent(scrollPane)));
    }

    public void setHeadline(String headline) {
	headlineLabel.setText(headline);
    }

    public void setMessage(String message) {
	messageLabel.setText(message);
    }

    public String getMessage() {
	return messageLabel.getText();
    }

}
