package de.ecsec.ecard.client;

import de.ecsec.core.common.ECardConstants;
import de.ecsec.core.common.enums.EventType;
import de.ecsec.core.common.logging.LogManager;
import de.ecsec.core.common.util.Helper;
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
    private String endpointUrl;
    private String redirectUrl;
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
        setParams();
        worker = null;
        env = new ClientEnv();
        ifd = new IFD();
        env.setIFD(ifd);
        EstablishContext ecRequest = new EstablishContext();
        EstablishContextResponse ecResponse = ifd.establishContext(ecRequest);
        if (ecResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
            if (ecResponse.getContextHandle() != null) {
                ctx = ecResponse.getContextHandle();
                initialized = true;
            }
        }
        try {
            recognition = new CardRecognition(ifd, ctx);
        } catch (Exception ex) {
            _logger.logp(Level.SEVERE, this.getClass().getName(), "init()", ex.getMessage(), ex);
            initialized = false;
        }
        paos = new PAOS(endpointUrl);
        env.addTransport("0", paos);
        em = new EventManager(recognition, env, sessionId, ctx);
        env.setEventManager(em);
        sal = new MicroSAL(env);
        em.registerAllEvents(sal);
        jsec = new JSEventCallback(this);
        InitializeResponse initResponse = sal.initialize(new Initialize());
        if (initResponse.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
            initialized = false;
        }
        List<ConnectionHandleType> cHandles = sal.getConnectionHandles();
        ConnectionHandleType cHandle;
        for (Iterator<ConnectionHandleType> iter = cHandles.iterator(); iter.hasNext(); ) {
            cHandle = iter.next();
            jsec.signalEvent(EventType.TERMINAL_ADDED, cHandle);
            if (cHandle.getRecognitionInfo() != null) {
                jsec.signalEvent(EventType.CARD_INSERTED, cHandle);
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
                worker = new Thread(new AppletWorker(this));
                worker.start();
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

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
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
        String param = getParameter("sessionId");
        // Session Id
        if (param != null) {
            sessionId = param;
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "sessionId set to " + param + ".", param);
            }
        } else {
            paramsPresent = false;
            _logger.logp(Level.SEVERE, this.getClass().getName(), "setParams()", "sessionId not set.");
        }
        // Endpoint URL
        param = getParameter("endpointUrl");
        if (param != null) {
            endpointUrl = param;
            if (_logger.isLoggable(Level.CONFIG)) {
                _logger.logp(Level.CONFIG, this.getClass().getName(), "setParams()", "endpointUrl set to " + param + ".", param);
            }
        } else {
            paramsPresent = false;
            _logger.logp(Level.SEVERE, this.getClass().getName(), "setParams()", "endpointUrl not set.");
        }
        // Redirect URL
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
        // wait for card
        param = getParameter("waitForCard");
        if (param != null) {
            waitForCard = Boolean.parseBoolean(param);
        }
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "setParams()");
        }
    }
}
