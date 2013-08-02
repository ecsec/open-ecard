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

package org.openecard.richclient;

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.security.Policy;
import javax.swing.JOptionPane;
import org.openecard.addon.AddonManager;
import org.openecard.addon.ClasspathRegistry;
import org.openecard.addon.manifest.AddonBundleDescription;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.SALStateCallback;
import org.openecard.common.util.FileUtils;
import org.openecard.control.ControlInterface;
import org.openecard.control.binding.http.HTTPBinding;
import org.openecard.control.handler.ControlHandlers;
import org.openecard.control.module.status.StatusAction;
import org.openecard.control.module.tctoken.TCTokenAction;
import org.openecard.event.EventManager;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.openecard.gui.swing.common.GUIDefaults;
import org.openecard.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.ifd.scio.IFD;
import org.openecard.management.TinyManagement;
import org.openecard.plugins.PluginPolicy;
import org.openecard.plugins.manager.PluginManager;
import org.openecard.plugins.pinplugin.PINPlugin;
import org.openecard.recognition.CardRecognition;
import org.openecard.richclient.gui.AppTray;
import org.openecard.richclient.gui.MessageDialog;
import org.openecard.sal.TinySAL;
import org.openecard.sal.protocol.eac.EAC2ProtocolFactory;
import org.openecard.sal.protocol.eac.EACGenericProtocolFactory;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ch.qos.logback.core.joran.spi.JoranException;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public final class RichClient {

    private static final Logger _logger = LoggerFactory.getLogger(RichClient.class.getName());
    private static final I18n lang = I18n.getTranslation("richclient");

    // Rich client
    private static RichClient client;
    // Tray icon
    private AppTray tray;
    // Control interface
    private ControlInterface control;
    // Client environment
    private ClientEnv env = new ClientEnv();
    // Interface Device Layer (IFD)
    private IFD ifd;
    // Service Access Layer (SAL)
    private TinySAL sal;
    // Event manager
    private EventManager em;
    // Card recognition
    private CardRecognition recognition;
    // card states
    private CardStateMap cardStates;
    // ContextHandle determines a specific IFD layer context
    private byte[] contextHandle;
    // Plugin manager
    private PluginManager pluginManager;


    static {
	try {
	    // load logger config from HOME if set
	    LogbackConfig.load();
	} catch (IOException ex) {
	    _logger.error("Failed to load logback config from user config.", ex);
	} catch (JoranException ex) {
	    _logger.error("Failed to load logback config from user config.", ex);
	}
    }


    public static void main(String[] args) {
	RichClient.getInstance();
    }

    public static synchronized RichClient getInstance() {
	if (client == null) {
	    client = new RichClient();
	    client.setup();
	}
	return client;
    }

    public void setup() {
	GUIDefaults.initialize();

	MessageDialog dialog = new MessageDialog();
	dialog.setHeadline(lang.translationForKey("client.startup.failed.headline"));

	try {

	    tray = new AppTray(this);
	    tray.beginSetup();

	    // Set up client environment
	    env = new ClientEnv();

	    // Set up Management
	    TinyManagement management = new TinyManagement(env);
	    env.setManagement(management);

	    // Set up the IFD
	    ifd = new IFD();
	    ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	    env.setIFD(ifd);

	    // Set up the Dispatcher
	    MessageDispatcher dispatcher = new MessageDispatcher(env);
	    env.setDispatcher(dispatcher);
	    ifd.setDispatcher(dispatcher);

	    // Perform an EstablishContext to get a ContextHandle
	    EstablishContext establishContext = new EstablishContext();
	    EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);
	    WSHelper.checkResult(establishContextResponse);
	    contextHandle = ifd.establishContext(establishContext).getContextHandle();

	    // Set up CardRecognition
	    recognition = new CardRecognition(ifd, contextHandle);

	    // Set up EventManager
	    em = new EventManager(recognition, env, contextHandle);
	    env.setEventManager(em);

	    // Set up SALStateCallback
	    cardStates = new CardStateMap();
	    SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
	    em.registerAllEvents(salCallback);

	    // Set up SAL
	    sal = new TinySAL(env, cardStates);
	    sal.addProtocol(ECardConstants.Protocol.EAC_GENERIC, new EACGenericProtocolFactory());
	    sal.addProtocol(ECardConstants.Protocol.EAC2, new EAC2ProtocolFactory());
	    env.setSAL(sal);

	    // Set up GUI
	    SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
	    sal.setGUI(gui);
	    ifd.setGUI(gui);
	    recognition.setGUI(gui);

	    tray.endSetup(recognition);
	    em.registerAllEvents(tray.status());

	    // Initialize the EventManager
	    em.initialize();

	    registerAddOns();

	    // Start up control interface
	    try {
		HTTPBinding binding = new HTTPBinding(HTTPBinding.DEFAULT_PORT);
		binding.setAddonManager(new AddonManager(dispatcher, gui, cardStates, recognition, em, sal.getProtocolInfo()));
		ControlHandlers handler = new ControlHandlers();
		control = new ControlInterface(binding, handler);
		control.start();
	    } catch (BindException e) {
		dialog.setMessage(lang.translationForKey("client.startup.failed.portinuse"));
		throw e;
	    }

	    // Set up PluginManager
	    String pluginsPath = FileUtils.getHomeConfigDir() + File.separator + "plugins" + File.separator;
	    Policy.setPolicy(new PluginPolicy(pluginsPath));
	    System.setSecurityManager(new SecurityManager());
	    pluginManager = new PluginManager(dispatcher, gui, recognition, cardStates, pluginsPath);
	    pluginManager.addPlugin(new PINPlugin());

	} catch (Exception e) {
	    _logger.error(e.getMessage(), e);

	    if (dialog.getMessage() == null || dialog.getMessage().isEmpty()) {
		// Add exception message if no custom message is set
		dialog.setMessage(e.getMessage());
	    }

	    // Show dialog to the user and shut down the client
	    JOptionPane.showMessageDialog(null, dialog, "Open eCard App", JOptionPane.PLAIN_MESSAGE);
	    teardown();
	}
    }

    private void registerAddOns() throws WSMarshallerException, MarshallingTypeException, IOException, SAXException {
	WSMarshaller marshaller = WSMarshallerFactory.createInstance();
	marshaller.addXmlTypeClass(AddonBundleDescription.class);
	InputStream manifestStream = FileUtils.resolveResourceAsStream(TCTokenAction.class, "TCToken-Manifest.xml");
	Document manifestDoc = marshaller.str2doc(manifestStream);
	ClasspathRegistry.getInstance().register((AddonBundleDescription) marshaller.unmarshal(manifestDoc));
	manifestStream = FileUtils.resolveResourceAsStream(StatusAction.class, "Status-Manifest.xml");
	manifestDoc = marshaller.str2doc(manifestStream);
	ClasspathRegistry.getInstance().register((AddonBundleDescription) marshaller.unmarshal(manifestDoc));
    }

    public void teardown() {
	try {
	    // shutdown plugin manager
	    pluginManager.shutDown();

	    // shutdown control modules
	    control.stop();

	    // shutdwon event manager
	    em.terminate();

	    // shutdown SAL
	    Terminate terminate = new Terminate();
	    sal.terminate(terminate);

	    // shutdown IFD
	    ReleaseContext releaseContext = new ReleaseContext();
	    releaseContext.setContextHandle(contextHandle);
	    ifd.releaseContext(releaseContext);
	} catch (Exception ex) {
	    _logger.error("Failed to stop Richclient.", ex);
	}

	System.exit(0);
    }

}
