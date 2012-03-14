/*
 * Copyright 2012 Johannes Schmoelz ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.applet;

import iso.std.iso_iec._24727.tech.schema.*;
import java.awt.Container;
import java.awt.Frame;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.logging.LogManager;
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
import org.openecard.client.transport.paos.PAOSCallback;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TLSClientSocketFactory;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class ECardApplet extends JApplet {

    private static final Logger _logger = LogManager.getLogger(ECardApplet.class.getName());

    private AppletWorker worker;
    private ClientEnv env;
    private TinySAL sal;
    private IFD ifd;
    private CardRecognition recognition;
    private EventManager em;
    private PAOS paos;
    private JSEventCallback jsec;
    private TinyManagement management;
    
    private byte[] ctx;
    private boolean initialized;
    private boolean paramsPresent;

    // applet parameters
    private String sessionId;
    private String endpointUrl;
    private String redirectUrl;
    private String reportId;
    private String spBehavior;
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
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "init()");
        }
        initialized = false;
        paramsPresent = true;
        setParams();
        env = new ClientEnv();
        management = new TinyManagement(env);
        env.setManagement(management);
        env.setDispatcher(new MessageDispatcher(env));
	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper(findParentFrame()));
        ifd = new IFD();
        ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
        ifd.setGUI(gui);
        env.setIFD(ifd);
        EstablishContext ecRequest = new EstablishContext();
        EstablishContextResponse ecResponse = ifd.establishContext(ecRequest);
        if (ecResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
            if (ecResponse.getContextHandle() != null) {
                ctx = ecResponse.getContextHandle();
                initialized = true;
            }
        }
        if (recognizeCard) {
            try {
                // TODO: reactivate remote tree repository as soon as it supports the embedded TLSMarker
                //GetRecognitionTree client = (GetRecognitionTree) WSClassLoader.getClientService(RecognitionProperties.getServiceName(), RecognitionProperties.getServiceAddr());
                recognition = new CardRecognition(ifd, ctx);
            } catch (Exception ex) {
                _logger.logp(Level.SEVERE, this.getClass().getName(), "init()", ex.getMessage(), ex);
                recognition = null;
                initialized = false;
            }
        } else {
            recognition = null;
        }
	// TODO: replace socket factory with this strange psk stuff
	PAOSCallback paosCallback = new PAOSCallback() {
	    
	    @Override
	    public void loadRefreshAddress() {
		
		new Thread(new Runnable() {
		    @Override
		    public void run() {
			try {
			    URL url = new URL(redirectUrl);
			    if(url.getQuery() != null){
				redirectUrl = redirectUrl + "&ResultMajor=ok";
			    } else  {
				redirectUrl = redirectUrl + "?ResultMajor=ok";
			    }
			    System.out.println("redirecting to: " + redirectUrl);
			ECardApplet.this.getAppletContext().showDocument(new URL(redirectUrl), "_blank");
			} catch (MalformedURLException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace(System.err);
			}
		    }
		}).start();	
	    }
	};

	String hostName = null;
	try {
	    hostName = new URL(endpointUrl).getHost();
	} catch (MalformedURLException ex) {
	    // TODO: find out what to do in case of this error
	}
	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(sessionId.getBytes(), Hex.decode(psk), hostName);
        paos = new PAOS(endpointUrl, env.getDispatcher(), paosCallback, new TLSClientSocketFactory(tlsClient));
        em = new EventManager(recognition, env, ctx, sessionId);
        env.setEventManager(em);
        sal = new TinySAL(env, sessionId);
	sal.setGUI(gui);
	sal.addProtocol(ECardConstants.Protocol.EAC, new EACProtocolFactory());
	env.setSAL(sal);   
	
        em.registerAllEvents(sal);
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
        InitializeResponse initResponse = sal.initialize(new Initialize());
        if (initResponse.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
            initialized = false;
        }
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

        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "init()");
        }
    }

    @Override
    public void start() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "start()");
        }
        if (paramsPresent && initialized) {
            if (worker == null) {
                worker = new AppletWorker(this);
            }
            worker.start();
        }
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "start()");
        }
    }

    @Override
    public void destroy() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "destroy()");
        }
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
        paos = null;
        if (em != null) {
            em.terminate();
            em = null;
        }
        sal = null;
        recognition = null;
        if (ifd != null) {
            ReleaseContext rcRequest = new ReleaseContext();
            rcRequest.setContextHandle(ctx);
            // Since JVM will be shut down after destroy method has been called by the browser,
            // evaluation of ReleaseContextResponse is not necessary.
            ifd.releaseContext(rcRequest);
            ifd = null;
        }
        env = null;
        jsec = null;
        ctx = null;
        sessionId = null;
        endpointUrl = null;
        redirectUrl = null;
        reportId = null;
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "destroy()");
        }
    }
    
    public Frame findParentFrame() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "findParentFrame()");
        }
        Container c = this;
        while (c != null) {
            if (c instanceof Frame) {
                if (_logger.isLoggable(Level.FINER)) {
                    _logger.exiting(this.getClass().getName(), "findParentFrame()", (Frame) c);
                }
                return (Frame) c;
            }
            c = c.getParent();
        }
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "findParentFrame()", (Frame) null);
        }
        return (Frame) null;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getReportId() {
        return reportId;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public boolean recognizeCard() {
        return recognizeCard;
    }

    public ClientEnv getEnv() {
        return env;
    }

    public TinySAL getTinySAL() {
        return sal;
    }

    public PAOS getPAOS() {
        return paos;
    }

    public String getSpBehavior() {
        return spBehavior;
    }

    public String getPsk() {
	return psk;
    }

    public void setPsk(String psk) {
	this.psk = psk;
    }

    public void startPAOS() {
        startPAOS(null);
    }

    public void startPAOS(String ifdName) {
        if (spBehavior.equals(CLICK)) {
            worker.startPAOS(ifdName);
        }
    }

    private void setParams() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "setParams()");
        }

        //
        // mandatory parameters
        //

        String param = getParameter("sessionId");
        if (param != null) {
            sessionId = param;
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "sessionId set to " + param + ".", param);
            }
        } else {
            _logger.logp(Level.SEVERE, this.getClass().getName(), "setParams()", "sessionId not set.");
            paramsPresent = false;
            return;
        }
        param = getParameter("endpointUrl");
        if (param != null) {
            endpointUrl = param;
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "endpointUrl set to " + param + ".", param);
            }
        } else {
            _logger.logp(Level.SEVERE, this.getClass().getName(), "setParams()", "endpointUrl not set.");
            paramsPresent = false;
            return;
        }

        param = getParameter("PSK");
        if (param != null) {
            setPsk(param);
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "PSK set to " + param + ".", param);
            }
        } else {
            _logger.logp(Level.SEVERE, this.getClass().getName(), "setParams()", "PSK not set.");
            paramsPresent = false;
            return;
        }
        
        //
        // optional parameters
        //

        param = getParameter("reportId");
        if (param != null) {
            reportId = param;
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "reportId set to " + param + ".", param);
            }
        } else {
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "reportId not set.");
            }
        }
        param = getParameter("redirectUrl");
        if (param != null) {
            redirectUrl = param;
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "redirectUrl set to " + param + ".", param);
            }
        } else {
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "redirectUrl not set.");
            }
        }
        param = getParameter("recognizeCard");
        if (param != null) {
            recognizeCard = Boolean.parseBoolean(param);
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "recognizeCard set to " + param + ".", param);
            }
        } else {
            recognizeCard = true;
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "recognizeCard set to " + recognizeCard + ".");
            }
        }
        param = getParameter("spBehavior");
        if (param != null) {
            if (param.equalsIgnoreCase(WAIT) || param.equalsIgnoreCase(CLICK)) {
                spBehavior = param;
            } else {
                // if param is neither set to WAIT nor set to CLICK, set it to INSTANT
                spBehavior = INSTANT;
            }
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "spBehavior set to " + spBehavior + ".", param);
            }
        } else {
            spBehavior = INSTANT;
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "spBehavior set to " + spBehavior + ".");
            }
        }
        param = getParameter("selfSigned");
        if (param != null) {
            selfSigned = Boolean.parseBoolean(param);
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "selfSigned set to " + param + ".", param);
            }
        } else {
            selfSigned = false;
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "selfSigned set to " + selfSigned + ".");
            }
        }

        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "setParams()");
        }
    }

  /*  private SSLSocketFactory createSSLSocketFactory() {
        if (selfSigned) {
            
            //
            // quick and dirty hack to support self-signed certificates
            //
            
            SSLContext sslCtx = null;
            try {
                sslCtx = SSLContext.getInstance("TLS");
            } catch (NoSuchAlgorithmException ex) {
                if (_logger.isLoggable(Level.WARNING)) {
                    _logger.logp(Level.WARNING, this.getClass().getName(), "createSSLSocketFactory()", ex.getMessage(), ex);
                    _logger.logp(Level.WARNING, this.getClass().getName(), "createSSLSocketFactory()", "Support for self-signed certificates disabled. Using default SSLSocketFactory.");
                }
                return SSLSocketFactory.getSocketFactory();
            }
            X509TrustManager tm = new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    // do nothing here...
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    // do nothing here...
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            try {
                sslCtx.init(null, new TrustManager[]{tm}, null);
            } catch (KeyManagementException ex) {
                if (_logger.isLoggable(Level.WARNING)) {
                    _logger.logp(Level.WARNING, this.getClass().getName(), "createSSLSocketFactory()", ex.getMessage(), ex);
                    _logger.logp(Level.WARNING, this.getClass().getName(), "createSSLSocketFactory()", "Support for self-signed certificates disabled. Using default SSLSocketFactory.");
                }
                return SSLSocketFactory.getSocketFactory();
            }
            SSLSocketFactory fac = new SSLSocketFactory(sslCtx);
            fac.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return fac;
        } else {
            return SSLSocketFactory.getSocketFactory();
        }
    }*/
    
}
