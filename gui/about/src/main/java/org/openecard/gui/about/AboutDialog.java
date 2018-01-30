/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

package org.openecard.gui.about;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
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
import org.openecard.common.I18n;
import org.openecard.common.AppVersion;
import org.openecard.common.util.StringUtils;
import org.openecard.common.util.SysUtils;
import org.openecard.gui.graphics.GraphicsUtil;
import org.openecard.gui.graphics.OecLogo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used to create a Swing based about dialog.
 * The dialog is localized with the {@code about} properties from the i18n module and the HTML pages in this modules'
 * {@code openecard_i18n/about} directory.
 *
 * @author Johannes Schmölz
 * @author Tobias Wich
 */
public class AboutDialog extends JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(AboutDialog.class);
    private static final long serialVersionUID = 1L;
    private static final I18n LANG = I18n.getTranslation("about");

    public static final String ABOUT_TAB = "about";
    public static final String FEEDBACK_TAB = "feedback";
    public static final String LICENSE_TAB = "license";
    public static final String SUPPORT_TAB = "support";

    private static AboutDialog runningDialog;

    private final HashMap<String, Integer> tabIndices = new HashMap<>();
    private JTabbedPane tabbedPane;

    static {
	try {
	    // create user.home.url property
	    String userHome = System.getProperty("user.home");
	    File f = new File(userHome);
	    // strip file:// as this must be written in the html file
	    String userHomeUrl = f.toURI().toString().substring(5);
	    LOG.debug("user.home.url = {}", userHomeUrl);
	    System.setProperty("user.home.url", userHomeUrl);
	} catch (SecurityException ex) {
	    LOG.error("Failed to calculate property 'user.home.url'.", ex);
	}
    }

    /**
     * Creates a new instance of this class.
     */
    private AboutDialog() {
	super();
	setupUI();
    }

    /**
     * Shows an about dialog and selects the specified index.
     * This method makes sure, that there is only one about dialog.
     *
     * @param selectedTab The identifier of the tab which should be selected. Valid identifiers are defined as constants
     *   in this class.
     */
    public static void showDialog(@Nullable String selectedTab) {
	if (runningDialog == null) {
	    AboutDialog dialog = new AboutDialog();
	    dialog.addWindowListener(new WindowListener() {
		@Override
		public void windowOpened(WindowEvent e) { }
		@Override
		public void windowClosing(WindowEvent e) { }
		@Override
		public void windowClosed(WindowEvent e) {
		    runningDialog = null;
		}
		@Override
		public void windowIconified(WindowEvent e) { }
		@Override
		public void windowDeiconified(WindowEvent e) { }
		@Override
		public void windowActivated(WindowEvent e) { }
		@Override
		public void windowDeactivated(WindowEvent e) { }
	    });
	    dialog.setVisible(true);
	    runningDialog = dialog;
	} else {
	    runningDialog.toFront();
	}

	// select tab if it exists
	Integer idx = runningDialog.tabIndices.get(StringUtils.nullToEmpty(selectedTab));
	if (idx != null) {
	    try {
		runningDialog.tabbedPane.setSelectedIndex(idx);
	    } catch (ArrayIndexOutOfBoundsException ex) {
		LOG.error("Invalid index selected.");
	    }
	}
    }

    /**
     * Shows an about dialog.
     * This method makes sure, that there is only one about dialog.
     */
    public static void showDialog() {
	showDialog(ABOUT_TAB);
    }

    private void setupUI() {
	Image logo = GraphicsUtil.createImage(OecLogo.class, 147, 147);

	setSize(730, 480);
	// use null layout with absolute positioning
	getContentPane().setLayout(null);
	getContentPane().setBackground(Color.white);

	JTextPane txtpnHeading = new JTextPane();
	txtpnHeading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
	txtpnHeading.setEditable(false);
	txtpnHeading.setText(LANG.translationForKey("about.heading", AppVersion.getName()));
	txtpnHeading.setBounds(12, 12, 692, 30);
	getContentPane().add(txtpnHeading);

	JTextPane txtpnVersion = new JTextPane();
	txtpnVersion.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
	txtpnVersion.setEditable(false);
	txtpnVersion.setText(LANG.translationForKey("about.version", AppVersion.getVersion()));
	txtpnVersion.setBounds(12, 54, 692, 18);
	getContentPane().add(txtpnVersion);

	JLabel label = new JLabel();
	label.setHorizontalAlignment(SwingConstants.CENTER);
	label.setIcon(new ImageIcon(logo));
	label.setBounds(12, 84, 155, 320);
	getContentPane().add(label);

	tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	tabbedPane.setBounds(185, 84, 529, 320);
	tabbedPane.setBackground(Color.white);
	int tabIdx = 0;
	tabbedPane.addTab(LANG.translationForKey("about.tab.about"), createTabContent(ABOUT_TAB));
	tabIndices.put(ABOUT_TAB, tabIdx++);
	tabbedPane.addTab(LANG.translationForKey("about.tab.feedback"), createTabContent(FEEDBACK_TAB));
	tabIndices.put(FEEDBACK_TAB, tabIdx++);
	tabbedPane.addTab(LANG.translationForKey("about.tab.support"), createTabContent(SUPPORT_TAB));
	tabIndices.put(SUPPORT_TAB, tabIdx++);
	tabbedPane.addTab(LANG.translationForKey("about.tab.license"), createTabContent(LICENSE_TAB));
	tabIndices.put(LICENSE_TAB, tabIdx++);
	getContentPane().add(tabbedPane);

	JButton btnClose = new JButton(LANG.translationForKey("about.button.close"));
	btnClose.setBounds(587, 416, 117, 25);
	btnClose.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispose();
	    }
	});
	getContentPane().add(btnClose);

	setIconImage(logo);
	setTitle(LANG.translationForKey("about.title", AppVersion.getName()));
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	setResizable(false);
	setLocationRelativeTo(null);
    }

    private JPanel createTabContent(String resourceName) {
	HTMLEditorKit kit = new HTMLEditorKit();
	kit.setAutoFormSubmission(false); // don't follow form link, use hyperlink handler instead
	HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();

	JEditorPane editorPane = new JEditorPane();
	editorPane.setEditable(false);
	editorPane.setEditorKit(kit);
	editorPane.setDocument(doc);

	try {
	    URL url = LANG.translationForFile(resourceName, "html");
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
	    try {
		URL url = event.getURL();
		String urlStr = url.toExternalForm();
		urlStr = SysUtils.expandSysProps(urlStr);
		url = new URL(urlStr);

		boolean browserOpened = false;
		if (Desktop.isDesktopSupported()) {
		    URI uri = new URI(urlStr);
		    if ("file".equals(url.getProtocol()) && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
			try {
			    Desktop.getDesktop().open(new File(uri));
			    browserOpened = true;
			} catch (IOException ex) {
			    // failed to open browser
			    LOG.debug(ex.getMessage(), ex);
			}
		    } else if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
			    Desktop.getDesktop().browse(uri);
			    browserOpened = true;
			} catch (IOException ex) {
			    // failed to open browser
			    LOG.debug(ex.getMessage(), ex);
			}
		    }
		}
		if (! browserOpened) {
		    String openTool;
		    if (SysUtils.isUnix()) {
			openTool = "xdg-open";
		    } else if (SysUtils.isWin()) {
			openTool = "start";
		    } else {
			openTool = "open";
		    }
		    ProcessBuilder pb = new ProcessBuilder(openTool, urlStr);
		    try {
			pb.start();
		    } catch (IOException ex) {
			// failed to execute command
			LOG.debug(ex.getMessage(), ex);
		    }
		}
	    } catch (URISyntaxException | MalformedURLException ex) {
		// wrong syntax
		LOG.debug(ex.getMessage(), ex);
	    }
	}
    }

}
