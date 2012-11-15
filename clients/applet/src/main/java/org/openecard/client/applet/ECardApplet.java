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

package org.openecard.client.applet;

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
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.I18n;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.common.util.FileUtils;
import org.openecard.client.control.module.status.EventHandler;
import org.openecard.client.event.EventManager;
import org.openecard.client.gui.swing.SwingDialogWrapper;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.eac.EACProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
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

    private ClientEnv env;
    private TinySAL sal;
    private IFD ifd;
    private CardRecognition recognition;
    private CardStateMap cardStates;
    private EventManager em;
    private JSEventCallback jsCallback;
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
		JOptionPane.showMessageDialog(null, lang.translationForKey("ifd.context.error"), lang
			.translationForKey("error"), JOptionPane.ERROR_MESSAGE, getLogo());
		destroy();
		return;
	    }
	} else {
	    logger.error("EstablishContext failed.");
	    JOptionPane.showMessageDialog(null, lang.translationForKey("ifd.context.error"), lang
		    .translationForKey("error"), JOptionPane.ERROR_MESSAGE, getLogo());
	    destroy();
	    return;
	}

	// CardRecognition
	try {
	    recognition = new CardRecognition(ifd, contextHandle);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    JOptionPane.showMessageDialog(null, lang.translationForKey("recognition.error"), lang
		    .translationForKey("error"), JOptionPane.ERROR_MESSAGE, getLogo());
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
	sal.addProtocol(ECardConstants.Protocol.EAC, new EACProtocolFactory());
	env.setSAL(sal);

	// JavaScript Bridge
	jsCallback = new JSEventCallback(this, cardStates, dispatcher, new EventHandler(em), gui, recognition);

	// start EventManager
	em.initialize();
    }

    @Override
    public void start() {
	this.jsCallback.startEventPush();
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
	    if (jsCallback != null) {
		jsCallback.stop();
	    }
	} catch (Exception ex) {
	    logger.error("An exception occurred while destroying JSEventCallback.", ex);
	} finally {
	    jsCallback = null;
	}
	// destroy the remaining components
	env = null;
    }

    public CardStateMap getCardStates() {
	return this.cardStates;
    }

    public JSEventCallback getCallback() {
	return this.jsCallback;
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

}
