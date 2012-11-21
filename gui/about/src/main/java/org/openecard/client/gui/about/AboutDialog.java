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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.openecard.client.common.I18n;
import org.openecard.client.common.Version;
import org.openecard.client.gui.graphics.GraphicsUtil;
import org.openecard.client.gui.graphics.OecLogoBgWhite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used to create an About dialog. It shows some information about the application, e.g. the version and
 * the license of the application, how to contribute to the project, how to submit bug reports and so on.
 * 
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class AboutDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(AboutDialog.class);
    private static final long serialVersionUID = 1L;

    private final I18n lang = I18n.getTranslation("about");

    /**
     * Constructor of AboutDialog class.
     */
    public AboutDialog() {
	super();
	setupUI();
    }

    /**
     * Convenience method for showing an About dialog.
     * Since this method is static, there is no need to create an instance of AboutDialog to call it.
     */
    public static void showDialog() {
	AboutDialog dialog = new AboutDialog();
	dialog.setVisible(true);
    }

    private void setupUI() {
	Image logo = GraphicsUtil.createImage(OecLogoBgWhite.class, 147, 147);
        
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
	txtpnVersion.setText(lang.translationForKey("about.version", Version.getVersion()));
	txtpnVersion.setBounds(12, 54, 692, 18);
	getContentPane().add(txtpnVersion);

	JLabel label = new JLabel();
	label.setHorizontalAlignment(SwingConstants.CENTER);
	label.setIcon(new ImageIcon(logo));
	label.setBounds(12, 84, 155, 320);
	getContentPane().add(label);

	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	tabbedPane.setBounds(185, 84, 519, 320);
	tabbedPane.setBackground(Color.white);
	tabbedPane.addTab(lang.translationForKey("about.tab.about"), createTabContent("about"));
	tabbedPane.addTab(lang.translationForKey("about.tab.feedback"), createTabContent("feedback"));
	tabbedPane.addTab(lang.translationForKey("about.tab.join"), createTabContent("join"));
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

	setIconImage(logo);
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
	    URL url = lang.translationForFile(resourceName, "html");
	    editorPane.setPage(url);
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
                        logger.debug(ex.getMessage(), ex);
		    }
		}
		if (! browserOpened) {
		    ProcessBuilder pb = new ProcessBuilder("xdg-open", uri.toString());
		    try {
			pb.start();
		    } catch (IOException ex) {
			// failed to execute command
                        logger.debug(ex.getMessage(), ex);
		    }
		}
	    } catch (URISyntaxException ex) {
		// wrong syntax
                logger.debug(ex.getMessage(), ex);
	    }
	}
    }

}
