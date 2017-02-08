/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import ch.qos.logback.core.joran.spi.JoranException;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.common.I18n;
import org.openecard.common.util.FileUtils;
import org.openecard.richclient.LogbackConfig;
import org.openecard.richclient.gui.manage.SettingsFactory;
import org.openecard.richclient.gui.manage.SettingsGroup;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * Custom settings group for logging settings.
 * The settings are made dynamic to reflect the choice made by the user.
 *
 * @author Tobias Wich
 */
public class LogSettingsGroup extends SettingsGroup {

    private static final long serialVersionUID = 1L;
    private static final I18n lang = I18n.getTranslation("addon");
    private static final Logger LOG = LoggerFactory.getLogger(LogSettingsGroup.class);

    private static final String GROUP         = "addon.core.logging.group_name";
    private static final String ROOT_NAME     = "addon.core.logging.root";
    private static final String ROOT_DESC     = "addon.core.logging.root.desc";
    private static final String ROOT_KEY      = "logging.root";
    private static final String PAOS_NAME     = "addon.core.logging.paos";
    private static final String PAOS_DESC     = "addon.core.logging.paos.desc";
    private static final String PAOS_KEY      = "org.openecard.transport.paos";
    private static final String EAC_NAME      = "addon.core.logging.eac";
    private static final String EAC_DESC      = "addon.core.logging.eac.desc";
    private static final String EAC_KEY       = "org.openecard.sal.protocol.eac";
    private static final String PACE_NAME     = "addon.core.logging.pace";
    private static final String PACE_DESC     = "addon.core.logging.pace.desc";
    private static final String PACE_KEY      = "org.openecard.ifd.protocol.pace";
    private static final String TRCHECKS_NAME = "addon.core.logging.trchecks";
    private static final String TRCHECKS_DESC = "addon.core.logging.trchecks.desc";
    private static final String TRCHECKS_KEY  = "org.openecard.common.util.TR03112Utils";
    private static final String TCTOKEN_NAME  = "addon.core.logging.tctoken";
    private static final String TCTOKEN_DESC  = "addon.core.logging.tctoken.desc";
    private static final String TCTOKEN_KEY   = "org.openecard.binding.tctoken";
    private static final String EVENT_NAME    = "addon.core.logging.event";
    private static final String EVENT_DESC    = "addon.core.logging.event.desc";
    private static final String EVENT_KEY     = "org.openecard.event";
    private static final String HTTPBIND_NAME = "addon.core.logging.httpbind";
    private static final String HTTPBIND_DESC = "addon.core.logging.httpbind.desc";
    private static final String HTTPBIND_KEY  = "org.openecard.control.binding.http";
    private static final String ADDON_NAME    = "addon.core.logging.addon";
    private static final String ADDON_DESC    = "addon.core.logging.addon.desc";
    private static final String ADDON_KEY     = "org.openecard.addon";
    private static final String SALSTATE_NAME = "addon.core.logging.salstate";
    private static final String SALSTATE_DESC = "addon.core.logging.salstate.desc";
    private static final String SALSTATE_KEY  = "org.openecard.common.sal.state";

    private static final String MDLW_NAME        = "addon.core.logging.middleware";
    private static final String MDLW_DESC        = "addon.core.logging.middleware.desc";
    private static final String MDLW_KEY         = "org.openecard.mdlw.sal";
    private static final String MDLW_EVENT_NAME  = "addon.core.logging.middleware-event";
    private static final String MDLW_EVENT_DESC  = "addon.core.logging.middleware-event.desc";
    private static final String MDLW_EVENT_KEY   = "org.openecard.mdlw.event";

    public LogSettingsGroup() {
	super(lang.translationForKey(GROUP), SettingsFactory.getInstance(loadProperties()));

	addSelectionItem(lang.translationForKey(ROOT_NAME), lang.translationForKey(ROOT_DESC), ROOT_KEY,
		new String[] {"ERROR", "WARN", "INFO", "DEBUG"});
	addLogLevelBox(lang.translationForKey(PAOS_NAME), lang.translationForKey(PAOS_DESC), PAOS_KEY);
	addLogLevelBox(lang.translationForKey(EAC_NAME), lang.translationForKey(EAC_DESC), EAC_KEY);
	addLogLevelBox(lang.translationForKey(PACE_NAME), lang.translationForKey(PACE_DESC), PACE_KEY);
	addLogLevelBox(lang.translationForKey(TRCHECKS_NAME), lang.translationForKey(TRCHECKS_DESC), TRCHECKS_KEY);
	addLogLevelBox(lang.translationForKey(TCTOKEN_NAME), lang.translationForKey(TCTOKEN_DESC), TCTOKEN_KEY);
	addLogLevelBox(lang.translationForKey(EVENT_NAME), lang.translationForKey(EVENT_DESC), EVENT_KEY);
	addLogLevelBox(lang.translationForKey(HTTPBIND_NAME), lang.translationForKey(HTTPBIND_DESC), HTTPBIND_KEY);
	addLogLevelBox(lang.translationForKey(ADDON_NAME), lang.translationForKey(ADDON_DESC), ADDON_KEY);
	addLogLevelBox(lang.translationForKey(SALSTATE_NAME), lang.translationForKey(SALSTATE_DESC), SALSTATE_KEY);
	addLogLevelBox(lang.translationForKey(MDLW_NAME), lang.translationForKey(MDLW_DESC), MDLW_KEY);
	addLogLevelBox(lang.translationForKey(MDLW_EVENT_NAME), lang.translationForKey(MDLW_EVENT_DESC), MDLW_EVENT_KEY);
    }

    private JComboBox addLogLevelBox(String name, String desc, String key) {
	String[] levels = {"", "WARN", "INFO", "DEBUG"};
	return addSelectionItem(name, desc, key, levels);
    }

    @Override
    protected void saveProperties() throws IOException, SecurityException, AddonPropertiesException {
	try {
	    File confFile = LogbackConfig.getConfFile();

	    // create file if needed
	    //if (! confFile.exists()) {
	    { // overwrite with default settings
		InputStream is = FileUtils.resolveResourceAsStream(LogSettingsGroup.class, "/logback.xml");
		try (FileOutputStream os = new FileOutputStream(confFile)) {
		    byte[] buffer = new byte[4096];
		    int n;
		    while ((n = is.read(buffer)) > 0) {
			os.write(buffer, 0, n);
		    }
		}
	    }
	    // load file into a Document
	    WSMarshaller m = WSMarshallerFactory.createInstance();
	    m.removeAllTypeClasses();
	    FileInputStream fin = new FileInputStream(confFile);
	    Document conf = m.str2doc(fin);

	    // process root value
	    String val = properties.getProperty(ROOT_KEY);
	    val = (val != null) ? val : "ERROR";
	    setRootlevel(conf, val);
	    // process every value
	    val = properties.getProperty(PAOS_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, PAOS_KEY, val);
	    val = properties.getProperty(EAC_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, EAC_KEY, val);
	    val = properties.getProperty(PACE_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, PACE_KEY, val);
	    val = properties.getProperty(TRCHECKS_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, TRCHECKS_KEY, val);
	    val = properties.getProperty(TCTOKEN_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, TCTOKEN_KEY, val);
	    val = properties.getProperty(EVENT_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, EVENT_KEY, val);
	    val = properties.getProperty(HTTPBIND_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, HTTPBIND_KEY, val);
	    val = properties.getProperty(ADDON_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, ADDON_KEY, val);
	    val = properties.getProperty(SALSTATE_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, SALSTATE_KEY, val);
	    val = properties.getProperty(MDLW_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, MDLW_KEY, val);
	    val = properties.getProperty(MDLW_EVENT_KEY);
	    val = (val != null) ? val : "";
	    setLoglevel(conf, MDLW_EVENT_KEY, val);
	    try ( // write log to file
		    FileWriter w = new FileWriter(confFile)) {
		String confStr = m.doc2str(conf);
		w.write(confStr);
	    }
	    // reload log config
	    LogbackConfig.load();
	} catch (JoranException ex) {
	    throw new AddonPropertiesException(ex.getMessage(), ex);
	} catch (WSMarshallerException | SAXException | TransformerException ex) {
	    throw new IOException(ex.getMessage(), ex);
	}
    }

    private static Properties loadProperties() {
	try {
	    File confFile = LogbackConfig.getConfFile();
	    if (! confFile.exists()) {
		// no file yet
		return new Properties();
	    } else {
		// load file into a Document
		WSMarshaller m = WSMarshallerFactory.createInstance();
		m.removeAllTypeClasses();
		FileInputStream fin = new FileInputStream(confFile);
		Document conf = m.str2doc(fin);
		// fill the properties
		Properties p = new Properties();
		p.setProperty(ROOT_KEY, getRootlevel(conf));
		p.setProperty(PAOS_KEY, getLoglevel(conf, PAOS_KEY));
		p.setProperty(EAC_KEY, getLoglevel(conf, EAC_KEY));
		p.setProperty(PACE_KEY, getLoglevel(conf, PACE_KEY));
		p.setProperty(TRCHECKS_KEY, getLoglevel(conf, TRCHECKS_KEY));
		p.setProperty(TCTOKEN_KEY, getLoglevel(conf, TCTOKEN_KEY));
		p.setProperty(EVENT_KEY, getLoglevel(conf, EVENT_KEY));
		p.setProperty(HTTPBIND_KEY, getLoglevel(conf, HTTPBIND_KEY));
		p.setProperty(ADDON_KEY, getLoglevel(conf, ADDON_KEY));
		p.setProperty(SALSTATE_KEY, getLoglevel(conf, SALSTATE_KEY));
		p.setProperty(MDLW_KEY, getLoglevel(conf, MDLW_KEY));
		p.setProperty(MDLW_EVENT_KEY, getLoglevel(conf, MDLW_EVENT_KEY));
		return p;
	    }
	} catch (IOException | SAXException | WSMarshallerException | AddonPropertiesException ex) {
	    // something else went wrong
	    return new Properties();
	}
    }

    private static String getLoglevel(Document conf, String path) throws AddonPropertiesException {
	try {
	    XPath p = XPathFactory.newInstance().newXPath();
	    XPathExpression exp = p.compile(String.format("/configuration/logger[@name='%s']", path));
	    Element e = (Element) exp.evaluate(conf, XPathConstants.NODE);

	    // no element means NO level
	    if (e == null) {
		return "";
	    } else {
		return e.getAttribute("level");
	    }
	} catch (DOMException | XPathExpressionException ex) {
	    throw new AddonPropertiesException("Failed to operate on log config document.");
	}
    }

    private static void setLoglevel(Document conf, String path, String level) throws AddonPropertiesException {
	try {
	    XPath p = XPathFactory.newInstance().newXPath();
	    XPathExpression exp = p.compile(String.format("/configuration/logger[@name='%s']", path));
	    Element e = (Element) exp.evaluate(conf, XPathConstants.NODE);

	    // create new element
	    if (e == null) {
		if (!level.isEmpty()) {
		    e = conf.createElement("logger");
		    e.setAttribute("name", path);
		    e.setAttribute("level", level);
		    conf.getDocumentElement().appendChild(e);
		}
		return;
	    }

	    // we have a node let's see what to do next
	    if (level.isEmpty()) {
		// delete entry
		e.getParentNode().removeChild(e);
	    } else {
		// modify level
		e.setAttribute("level", level);
	    }
	} catch (DOMException | XPathExpressionException ex) {
	    throw new AddonPropertiesException("Failed to operate on log config document.");
	}
    }

    private static String getRootlevel(Document conf) throws AddonPropertiesException {
	try {
	    XPath p = XPathFactory.newInstance().newXPath();
	    XPathExpression exp = p.compile("/configuration/root");
	    Element e = (Element) exp.evaluate(conf, XPathConstants.NODE);
	    // no root is beyond our capability
	    if (e == null) {
		throw new AddonPropertiesException("Logging config is invalid, the root element is missing.");
	    }

	    return e.getAttribute("level");
	} catch (DOMException | XPathExpressionException ex) {
	    throw new AddonPropertiesException("Failed to operate on log config document.");
	}
    }

    private static void setRootlevel(Document conf, String level) throws AddonPropertiesException {
	try {
	    XPath p = XPathFactory.newInstance().newXPath();
	    XPathExpression exp = p.compile("/configuration/root");
	    Element e = (Element) exp.evaluate(conf, XPathConstants.NODE);
	    // no root is beyond our capability
	    if (e == null) {
		throw new AddonPropertiesException("Logging config is invalid, the root element is missing.");
	    }

	    e.setAttribute("level", level);
	} catch (DOMException | XPathExpressionException ex) {
	    throw new AddonPropertiesException("Failed to operate on log config document.");
	}
    }

    private JComponent createSupportPanel() {
	HTMLEditorKit kit = new HTMLEditorKit();
	HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();

	JEditorPane editorPane = new JEditorPane();
	editorPane.setEditable(false);
	editorPane.setEditorKit(kit);
	editorPane.setDocument(doc);
	//editorPane.setMaximumSize(new Dimension(520, 400));
	editorPane.setPreferredSize(new Dimension(520, 250));

	try {
	    URL url = I18n.getTranslation("richclient").translationForFile("debug", "html");
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

	return editorPane;
    }

    private void openUrl(HyperlinkEvent event) {
	HyperlinkEvent.EventType type = event.getEventType();
	if (type == HyperlinkEvent.EventType.ACTIVATED) {
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
			LOG.debug(ex.getMessage(), ex);
		    }
		}
		if (! browserOpened) {
		    ProcessBuilder pb = new ProcessBuilder("xdg-open", uri.toString());
		    try {
			pb.start();
		    } catch (IOException ex) {
			// failed to execute command
			LOG.debug(ex.getMessage(), ex);
		    }
		}
	    } catch (URISyntaxException ex) {
		// wrong syntax
		LOG.debug(ex.getMessage(), ex);
	    }
	}
    }

}
