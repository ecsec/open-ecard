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

package org.openecard.clients.applet;

import de.bund.bsi.ecard.api._1.TerminateFramework;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import java.awt.Container;
import java.awt.Frame;
import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JOptionPane;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.Version;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.SALStateCallback;
import org.openecard.common.util.FileUtils;
import org.openecard.control.module.status.EventHandler;
import org.openecard.event.EventManager;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.openecard.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.ifd.scio.IFD;
import org.openecard.management.TinyManagement;
import org.openecard.recognition.CardRecognition;
import org.openecard.sal.TinySAL;
import org.openecard.sal.protocol.eac.EAC2ProtocolFactory;
import org.openecard.sal.protocol.eac.EACGenericProtocolFactory;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public class ECardApplet extends JApplet {

    private static final Logger logger = LoggerFactory.getLogger(ECardApplet.class);
    private static final I18n lang = I18n.getTranslation("applet");
    private static final long serialVersionUID = 1L;

    private ClientEnv env;
    private TinySAL sal;
    private IFD ifd;
    private CardRecognition recognition;
    private CardStateMap cardStates;
    private EventManager em;
    private JSCommunicationHandler jsCommHandler;
    private TinyManagement management;
    private byte[] contextHandle;

    /**
     * Initialization method that will be called after the applet is loaded into
     * the browser.
     */
    @Override
    public void init() {
	try {
	    LogProperties.loadJavaUtilLogging();
	} catch (IOException ex) {
	    System.err.println("WARNING: Using java.util.logging system defaults.");
	}

	// Client environment
	env = new ClientEnv();

	// Management
	management = new TinyManagement(env);
	env.setManagement(management);

	// Dispatcher
	Dispatcher dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);

	// GUI
	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());

	// IFD
	ifd = new IFD();
	ifd.setDispatcher(dispatcher);
	ifd.setGUI(gui);
	ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	env.setIFD(ifd);

	EstablishContext establishContext = new EstablishContext();
	EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);
	if (establishContextResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
	    if (establishContextResponse.getContextHandle() != null) {
		contextHandle = establishContextResponse.getContextHandle();
	    } else {
		logger.error("EstablishContext failed.");
		JOptionPane.showMessageDialog(null, lang.translationForKey("ifd.context.error"),
			lang.translationForKey("error"), JOptionPane.ERROR_MESSAGE, getLogo());
		destroy();
		return;
	    }
	} else {
	    logger.error("EstablishContext failed.");
	    JOptionPane.showMessageDialog(null, lang.translationForKey("ifd.context.error"),
		    lang.translationForKey("error"), JOptionPane.ERROR_MESSAGE, getLogo());
	    destroy();
	    return;
	}

	// CardRecognition
	try {
	    recognition = new CardRecognition(ifd, contextHandle);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    JOptionPane.showMessageDialog(null, lang.translationForKey("recognition.error"),
		    lang.translationForKey("error"), JOptionPane.ERROR_MESSAGE, getLogo());
	    destroy();
	    return;
	}

	// EventManager
	em = new EventManager(recognition, env, contextHandle);
	env.setEventManager(em);

	// CardStateMap
	this.cardStates = new CardStateMap();
	SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
	em.registerAllEvents(salCallback);

	// SAL
	sal = new TinySAL(env, cardStates);
	sal.setGUI(gui);
	sal.addProtocol(ECardConstants.Protocol.EAC_GENERIC, new EACGenericProtocolFactory());
	sal.addProtocol(ECardConstants.Protocol.EAC2, new EAC2ProtocolFactory());
	env.setSAL(sal);

	// JavaScript Bridge
	EventHandler evt = new EventHandler(em);
	jsCommHandler = new JSCommunicationHandler(this, cardStates, dispatcher, evt, gui, sal.getProtocolInfo(), recognition);

	// start EventManager
	em.initialize();
    }

    @Override
    public void start() {
	jsCommHandler.sendStarted();
	jsCommHandler.sendMessage("Open eCard Applet started");
	jsCommHandler.startEventPush();
    }

    @Override
    public void stop() {
	jsCommHandler.sendMessage("Open eCard Applet stopped");
    }

    @Override
    public void destroy() {
	// destroy EventManager
	try {
	    if (em != null) {
		em.terminate();
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying EventManager.", ex);
	} finally {
	    em = null;
	    recognition = null;
	}
	// destroy Management
	try {
	    if (management != null) {
		TerminateFramework terminateFramework = new TerminateFramework();
		management.terminateFramework(terminateFramework);
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying Management.", ex);
	} finally {
	    management = null;
	}
	// destroy SAL
	try {
	    if (sal != null) {
		Terminate terminate = new Terminate();
		sal.terminate(terminate);
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying SAL.", ex);
	} finally {
	    sal = null;
	    cardStates = null;
	}
	// destroy IFD
	try {
	    if (ifd != null) {
		ReleaseContext releaseContext = new ReleaseContext();
		releaseContext.setContextHandle(contextHandle);
		ifd.releaseContext(releaseContext);
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying IFD.", ex);
	} finally {
	    ifd = null;
	    contextHandle = null;
	}
	// destroy JSEventCallback
	try {
	    if (jsCommHandler != null) {
		jsCommHandler.stop();
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying JSCommunicationHandler.", ex);
	} finally {
	    jsCommHandler = null;
	}
	// destroy the remaining components
	env = null;
    }

    public CardStateMap getCardStates() {
	return cardStates;
    }

    public JSCommunicationHandler getJSCommunicationHandler() {
	return jsCommHandler;
    }

    public Frame findParentFrame() {
	Container c = this;
	while (c != null) {
	    if (c instanceof Frame) {
		return (Frame) c;
	    }
	    c = c.getParent();
	}
	return (Frame) null;
    }

    private ImageIcon getLogo() {
	URL resource = FileUtils.resolveResourceAsURL(ECardApplet.class, "/images/logo.png");
	return new ImageIcon(resource);
    }

    @Override
    public String getAppletInfo() {
	String info = "Open eCard App (" + Version.getVersion() + ")\n"
		+ "http://www.openecard.org\n"
		+ "Copyright (C) 2012-2013 ecsec GmbH\n"
		+ "All rights reserved.\n"
		+ "\n"
		+ "This software is distributed under the terms of the GNU General Public License Version 3.\n"
		+ "https://www.gnu.org/licenses/gpl-3.0.txt";

	return info;
    }

}
