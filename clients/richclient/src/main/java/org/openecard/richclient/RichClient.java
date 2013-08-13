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

import ch.qos.logback.core.joran.spi.JoranException;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import java.io.IOException;
import java.net.BindException;
import javax.swing.JOptionPane;
import org.openecard.addon.AddonManager;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.SALStateCallback;
import org.openecard.control.binding.http.HTTPBinding;
import org.openecard.event.EventManager;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.openecard.gui.swing.common.GUIDefaults;
import org.openecard.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.ifd.scio.IFD;
import org.openecard.management.TinyManagement;
import org.openecard.recognition.CardRecognition;
import org.openecard.richclient.gui.AppTray;
import org.openecard.richclient.gui.MessageDialog;
import org.openecard.sal.TinySAL;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public final class RichClient {

    private static final Logger _logger = LoggerFactory.getLogger(RichClient.class.getName());
    private static final I18n lang = I18n.getTranslation("richclient");

    // Tray icon
    private AppTray tray;
    // Control interface
    private HTTPBinding binding;
    // Client environment
    private ClientEnv env = new ClientEnv();
    // Interface Device Layer (IFD)
    private IFD ifd;
    // Service Access Layer (SAL)
    private TinySAL sal;
    // AddonManager
    private AddonManager manager;
    // Event manager
    private EventManager em;
    // Card recognition
    private CardRecognition recognition;
    // card states
    private CardStateMap cardStates;
    // ContextHandle determines a specific IFD layer context
    private byte[] contextHandle;

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
	RichClient client = new RichClient();
	client.setup();
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
	    env.setSAL(sal);

	    // Set up GUI
	    SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
	    sal.setGUI(gui);
	    ifd.setGUI(gui);
	    recognition.setGUI(gui);

	    // Start up control interface
	    try {
		binding = new HTTPBinding(HTTPBinding.DEFAULT_PORT);
		manager = new AddonManager(dispatcher, gui, cardStates, recognition, em);
		sal.setAddonManager(manager);
		binding.setAddonManager(manager);
		binding.start();
	    } catch (BindException e) {
		dialog.setMessage(lang.translationForKey("client.startup.failed.portinuse"));
		throw e;
	    }

	    tray.endSetup(recognition, manager);

	    // Initialize the EventManager
	    em.registerAllEvents(tray.status());
	    em.initialize();

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

    public void teardown() {
	try {
	    // TODO shutdown addon manager and related components?

	    // shutdown control modules
	    binding.stop();

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
