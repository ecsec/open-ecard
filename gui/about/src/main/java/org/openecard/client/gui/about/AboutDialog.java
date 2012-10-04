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

package org.openecard.client.gui.about;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.openecard.client.common.I18n;
import org.openecard.client.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class AboutDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(AboutDialog.class);

    private final I18n lang = I18n.getTranslation("about");

    public AboutDialog() {
	super();
	setupUI();
    }

    public static void showDialog() {
	AboutDialog dialog = new AboutDialog();
	dialog.setVisible(true);
    }

    private void setupUI() {
	setSize(720, 480);
	// use null layout with absolute positioning
	getContentPane().setLayout(null);
	getContentPane().setBackground(Color.white);

	JTextPane txtpnHeading = new JTextPane();
	txtpnHeading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
	txtpnHeading.setEditable(false);
	txtpnHeading.setText(lang.translationForKey("about.heading"));
	txtpnHeading.setBounds(12, 12, 692, 30);
	getContentPane().add(txtpnHeading);

	JTextPane txtpnVersion = new JTextPane();
	txtpnVersion.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
	txtpnVersion.setEditable(false);
	txtpnVersion.setText(lang.translationForKey("about.version"));
	txtpnVersion.setBounds(12, 54, 692, 18);
	getContentPane().add(txtpnVersion);

	JLabel label = new JLabel();
	label.setHorizontalAlignment(SwingConstants.CENTER);
	label.setIcon(getImageIcon("oec_logo_bg-white.png"));
	label.setBounds(12, 84, 155, 320);
	getContentPane().add(label);

	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	tabbedPane.setBounds(185, 84, 519, 320);
	tabbedPane.setBackground(Color.white);
	tabbedPane.addTab(lang.translationForKey("about.tab.about"), createTabContent("about_de.html"));
	tabbedPane.addTab(lang.translationForKey("about.tab.feedback"), createTabContent("feedback_de.html"));
	tabbedPane.addTab(lang.translationForKey("about.tab.join"), createTabContent("join_de.html"));
	getContentPane().add(tabbedPane);

	JButton btnClose = new JButton(lang.translationForKey("about.button.close"));
	btnClose.setBounds(587, 416, 117, 25);
	btnClose.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispose();
	    }
	});
	getContentPane().add(btnClose);

	setIconImage(getImageIcon("oec_logo_bg-white.png").getImage());
	setTitle(lang.translationForKey("about.title"));
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	setResizable(false);
	setLocationRelativeTo(null);
    }

    private JPanel createTabContent(String resourceName) {
	HTMLEditorKit kit = new HTMLEditorKit();
	HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();

	JEditorPane editorPane = new JEditorPane();
	editorPane.setEditable(false);
	editorPane.setEditorKit(kit);
	editorPane.setDocument(doc);

	try {
	    editorPane.setPage(getResourceUrl(resourceName));
	} catch (IOException ex) {
	    editorPane.setText("Page not found.");
	}

	editorPane.addHyperlinkListener(new HyperlinkListener() {

	    @Override
	    public void hyperlinkUpdate(HyperlinkEvent e) {
		openUrl(e);
	    }
	});

	JScrollPane scrollPane = new JScrollPane(editorPane);

	JPanel panel = new JPanel();
	panel.setLayout(new BorderLayout());
	panel.add(scrollPane, BorderLayout.CENTER);

	return panel;
    }

    private ImageIcon getImageIcon(String name) {
	URL imageUrl = FileUtils.resolveResourceAsURL(AboutDialog.class, "about/images/" + name);
	ImageIcon icon = new ImageIcon(imageUrl);
	return icon;
    }

    private URL getResourceUrl(String name) {
	URL resourceUrl = FileUtils.resolveResourceAsURL(AboutDialog.class, "about/html/" + name);
	return resourceUrl;
    }

    private void openUrl(HyperlinkEvent event) {
	EventType type = event.getEventType();
	if (type == EventType.ACTIVATED) {
	    String url = event.getURL().toExternalForm();
	    try {
		boolean browserOpened = false;
		URI uri = new URI(url);
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		    try {
			Desktop.getDesktop().browse(uri);
			browserOpened = true;
		    } catch (IOException ex) {
			// failed to open browser
		    }
		}
		if (! browserOpened) {
		    ProcessBuilder pb = new ProcessBuilder("xdg-open", uri.toString());
		    try {
			pb.start();
		    } catch (IOException ex) {
			// failed to execute command
		    }
		}
	    } catch (URISyntaxException ex) {
		// wrong syntax
	    }
	}
    }

}
