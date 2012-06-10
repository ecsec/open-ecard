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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import java.awt.Container;
import java.awt.Frame;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.swing.JApplet;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.connector.Connector;
import org.openecard.client.event.EventManager;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.eac.EACProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.openecard.client.transport.paos.PAOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ECardApplet extends JApplet {

    private static final Logger logger = LoggerFactory.getLogger(ECardApplet.class);
    private AppletWorker worker;
    private ClientEnv env;
    private TinySAL sal;
    private IFD ifd;
    private CardRecognition recognition;
    private CardStateMap cardStates;
    private EventManager em;
    private PAOS paos;
    private JSEventCallback jsec;
    private TinyManagement management;
    private byte[] contextHandle;
    private boolean initialized = false;
    // applet parameters
    private String sessionID;
    private URL endpointURL;
    private URL redirectURL;
    private String reportID;
    private String spBehavior = INSTANT;
    private String psk;
    private boolean recognizeCard;
    private boolean selfSigned;
    protected static final String INSTANT = "instant";
    protected static final String WAIT = "wait";
    protected static final String CLICK = "click";


    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    @Override
    public void init() {
	loadParameters();


	// Client environment
	env = new ClientEnv();

	// Management
	management = new TinyManagement(env);
	env.setManagement(management);

	// Dispatcher
	Dispatcher dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);

	// GUI
	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper(findParentFrame()));

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
		throw new RuntimeException("Cannot establish context");
	    }
	} else {
	    throw new RuntimeException("Cannot establish context");
	}

	if (recognizeCard) {
	    try {
		// TODO: reactivate remote tree repository as soon as it supports the embedded TLSMarker
		//GetRecognitionTree client = (GetRecognitionTree) WSClassLoader.getClientService(RecognitionProperties.getServiceName(), RecognitionProperties.getServiceAddr());
		recognition = new CardRecognition(ifd, contextHandle);
	    } catch (Exception ex) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
		logger.error(LoggingConstants.THROWING, "Exception", ex);
		// </editor-fold>
		initialized = false;
	    }
	}

	// EventManager
	em = new EventManager(recognition, env, contextHandle, sessionID);
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


	jsec = new JSEventCallback(this);
	em.registerAllEvents(jsec);

	worker = new AppletWorker(this);
	if (spBehavior.equals(WAIT)) {
	    if (recognizeCard) {
		em.register(worker, EventType.CARD_RECOGNIZED);
	    } else {
		em.register(worker, EventType.CARD_INSERTED);
	    }
	}

	em.initialize();

	List<ConnectionHandleType> cHandles = sal.getConnectionHandles();
	if (cHandles.isEmpty()) {
	    jsec.showMessage("Please connect Terminal.");
	} else {
	    ConnectionHandleType cHandle;
	    for (Iterator<ConnectionHandleType> iter = cHandles.iterator(); iter.hasNext();) {
		cHandle = iter.next();
		jsec.signalEvent(EventType.TERMINAL_ADDED, cHandle);
		if (cHandle.getRecognitionInfo() != null) {
		    jsec.signalEvent(EventType.CARD_INSERTED, cHandle);
		}
	    }
	}

	/* TODO: remove obsolete code
	// PAOS
	if (psk != null) {
	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(sessionID.getBytes(), Hex.decode(psk), endpointURL.getHost());
	paos = new PAOS(endpointURL, env.getDispatcher(), new TlsClientSocketFactory(tlsClient));
	} else {
	paos = new PAOS(endpointURL, env.getDispatcher());
	}
	*/

	// Start client connector to listen on port 24727
	try {

	    Connector connector = Connector.getInstance();
	    connector.addConnectorListener(worker);
	} catch (Exception e) {
	    throw new RuntimeException("Client connector is running.");
	}
    }

    @Override
    public void start() {
	if (initialized) {
	    if (worker == null) {
		worker = new AppletWorker(this);
	    }
	    worker.start();
	}
    }

    @Override
    public void destroy() {
	try {
	    worker.interrupt();
	} catch (Exception ignore) {
	}

	try {
	    em.terminate();
	} catch (Exception ignore) {
	}

	try {
	    ReleaseContext releaseContext = new ReleaseContext();
	    releaseContext.setContextHandle(contextHandle);
	    ifd.releaseContext(releaseContext);
	} catch (Exception ignore) {
	}
    }

    public CardStateMap getCardStates() {
    	return this.cardStates;
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

    public String getSessionID() {
	return sessionID;
    }

    public String getReportID() {
	return reportID;
    }

    public ClientEnv getClientEnvironment() {
	return env;
    }

    public PAOS getPAOS() {
	return paos;
    }

    public String getSpBehavior() {
	return spBehavior;
    }

    public void startPAOS() {
	startPAOS(null);
    }

    public void startPAOS(String ifdName) {
	if (spBehavior.equals(CLICK)) {
	    worker.startPAOS(ifdName);
	}
    }

    private void loadParameters() {

	///
	/// MANDATORY parameters
	///

	/*
	 * sessionID is not used anymore, but we've to set it to something
	 * to satisfy current EventManager implementation
	 */
	sessionID = ValueGenerators.generateSessionID();
	/*
	  sessionID = getParameter("sessionId");
	  if (sessionID == null) {
	  throw new IllegalArgumentException("The parameter sessionID is missing!");
	  }
	  // <editor-fold defaultstate="collapsed" desc="log configuration">
	  logger.debug(LoggingConstants.CONFIG, "sessionId set to {}", sessionID);
	  // </editor-fold>
	  */

	/* endpointURL is not needed anymore
	   try {
	   endpointURL = new URL(getParameter("endpointUrl"));
	   } catch (Exception e) {
	   throw new IllegalArgumentException("Malformed endpointURL parameter: " + e.getMessage());
	   }
	   // <editor-fold defaultstate="collapsed" desc="log configuration">
	   logger.debug(LoggingConstants.CONFIG, "endpointUrl set to {}", endpointURL);
	   // </editor-fold>
	   */

	psk = getParameter("PSK");
	// <editor-fold defaultstate="collapsed" desc="log configuration">
	logger.debug(LoggingConstants.CONFIG, "PSK set to {}", psk);
	// </editor-fold>

	///
	/// OPTIONAL parameters
	///

	reportID = getParameter("reportId");
	// <editor-fold defaultstate="collapsed" desc="log configuration">
	logger.debug(LoggingConstants.CONFIG, "reportId set to {}", reportID);
	// </editor-fold>

	try {
	    redirectURL = new URL(getParameter("redirectUrl"));
	} catch (Exception e) {
	    if (e.getCause() instanceof java.net.MalformedURLException) {
		throw new IllegalArgumentException("Malformed redirectURL parameter: " + e.getMessage());
	    }
	}
	// <editor-fold defaultstate="collapsed" desc="log configuration">
	logger.debug(LoggingConstants.CONFIG, "redirectUrl set to {}", redirectURL);
	// </editor-fold>

	String param = getParameter("recognizeCard");
	if (param != null) {
	    recognizeCard = Boolean.parseBoolean(param);
	} else {
	    recognizeCard = true;
	}
	// <editor-fold defaultstate="collapsed" desc="log configuration">
	logger.debug(LoggingConstants.CONFIG, "recognizeCard set to {}", recognizeCard);
	// </editor-fold>

	param = getParameter("spBehavior");
	if (param != null) {
	    if (param.equalsIgnoreCase(WAIT) || param.equalsIgnoreCase(CLICK) || param.equalsIgnoreCase(INSTANT)) {
		spBehavior = param;
	    } else {
		throw new IllegalArgumentException("The parameter spBehavior is malformed!");
	    }
	}
	// <editor-fold defaultstate="collapsed" desc="log configuration">
	logger.debug(LoggingConstants.CONFIG, "spBehavior set to {}", spBehavior);
	// </editor-fold>

	Boolean.parseBoolean(getParameter("selfSigned"));
	// <editor-fold defaultstate="collapsed" desc="log configuration">
	logger.debug(LoggingConstants.CONFIG, "selfSigned set to {}", selfSigned);
	// </editor-fold>
    }

}
