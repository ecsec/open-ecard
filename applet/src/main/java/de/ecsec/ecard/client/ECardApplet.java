package de.ecsec.ecard.client;

import de.ecsec.core.common.ECardConstants;
import de.ecsec.core.common.enums.EventType;
import de.ecsec.core.common.logging.LogManager;
import de.ecsec.core.environment.ClientEnv;
import de.ecsec.core.ifd.IFD;
import de.ecsec.core.recognition.CardRecognition;
import de.ecsec.core.sal.MicroSAL;
import de.ecsec.core.transport.paos.PAOS;
import de.ecsec.ecard.client.event.EventManager;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import iso.std.iso_iec._24727.tech.schema.InitializeResponse;
import java.awt.Container;
import java.awt.Frame;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;

/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class ECardApplet extends JApplet {

    private static final Logger _logger = LogManager.getLogger(ECardApplet.class.getName());
    
    private Thread worker;
    private ClientEnv env;
    private MicroSAL sal;
    private IFD ifd;
    private CardRecognition recognition;
    private EventManager em;
    private PAOS paos;
    private JSEventCallback jsec;
    
    private byte[] ctx;
    private boolean initialized;
    private boolean paramsPresent;
    
    // applet parameters
    private String sessionId;
    private String reportId;
    private String endpointUrl;
    private String redirectUrl;
    private boolean recognizeCard;
    private boolean waitForCard;

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
        System.out.println("ECardApplet :: setParams()");
        setParams();
        System.out.println("ECardApplet :: params present ? " + paramsPresent);
        worker = null;
        System.out.println("ECardApplet :: create ClientEnv");
        env = new ClientEnv();
        System.out.println("ECardApplet :: create IFD");
        ifd = new IFD();
        System.out.println("ECardApplet :: set IFD in ClientEnv");
        env.setIFD(ifd);
        System.out.println("ECardApplet :: establish Context");
        EstablishContext ecRequest = new EstablishContext();
        EstablishContextResponse ecResponse = ifd.establishContext(ecRequest);
        if (ecResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
            if (ecResponse.getContextHandle() != null) {
                ctx = ecResponse.getContextHandle();
                initialized = true;
            }
        }
        System.out.println("ECardApplet :: create CardRecognition");
        try {
            recognition = new CardRecognition(ifd, ctx);
        } catch (Exception ex) {
            _logger.logp(Level.SEVERE, this.getClass().getName(), "init()", ex.getMessage(), ex);
            initialized = false;
        }
        System.out.println("ECardApplet :: create PAOS");
        paos = new PAOS(endpointUrl);
        System.out.println("ECardApplet :: add PAOS to ClientEnv");
        env.addTransport("0", paos);
        System.out.println("ECardApplet :: create EventManager");
        em = new EventManager(recognition, env, sessionId, ctx, recognizeCard);
        System.out.println("ECardApplet :: set EventManager in ClientEnv");
        env.setEventManager(em);
        System.out.println("ECardApplet :: create MicroSAL");
        sal = new MicroSAL(env);
        System.out.println("ECardApplet :: register MicroSAL in EventManager");
        em.registerAllEvents(sal);
        System.out.println("ECardApplet :: create JSEventCallback");
        jsec = new JSEventCallback(this);
        System.out.println("ECardApplet :: register JSEventCallback in EventManager");
        em.registerAllEvents(jsec);
        System.out.println("ECardApplet :: initialize MicroSAL");
        InitializeResponse initResponse = sal.initialize(new Initialize());
        if (initResponse.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
            initialized = false;
        }
        System.out.println("ECardApplet :: get ConnectionHandles from MicroSAL");
        List<ConnectionHandleType> cHandles = sal.getConnectionHandles();
        ConnectionHandleType cHandle;
        for (Iterator<ConnectionHandleType> iter = cHandles.iterator(); iter.hasNext(); ) {
            cHandle = iter.next();
            jsec.signalEvent(EventType.TERMINAL_ADDED, cHandle);
            if (cHandle.getRecognitionInfo() != null) {
                jsec.signalEvent(EventType.CARD_INSERTED, cHandle);
            }
        }
        System.out.println("ECardApplet :: initialized ? " + initialized);
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
                System.out.println("ECardApplet :: create AppletWorker");
                worker = new Thread(new AppletWorker(this));
                System.out.println("ECardApplet :: start AppletWorker");
                worker.start();
                System.out.println("ECardApplet :: AppletWorker started");
            }
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
        sessionId = null;
        endpointUrl = null;
        redirectUrl = null;
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
    
    public boolean waitForCard() {
        return waitForCard;
    }

    public ClientEnv getEnv() {
        return env;
    }

    public MicroSAL getMicroSAL() {
        return sal;
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
        param = getParameter("waitForCard");
        if (param != null) {
            waitForCard = Boolean.parseBoolean(param);
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "waitForCard set to " + param + ".", param);
            }
        } else {
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "waitForCard not set.");
            }
        }
        
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "setParams()");
        }
    }
}
