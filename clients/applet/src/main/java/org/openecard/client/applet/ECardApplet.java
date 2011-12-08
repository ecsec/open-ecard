package org.openecard.client.applet;

import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.MicroSAL;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.event.EventManager;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import iso.std.iso_iec._24727.tech.schema.InitializeResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import java.awt.Container;
import java.awt.Frame;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.recognition.RecognitionProperties;
import org.openecard.client.ws.WSClassLoader;
import org.openecard.ws.GetRecognitionTree;


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
    private String reportId;
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
        setParams();
        worker = null;
        env = new ClientEnv();
        ifd = new IFD();
        ifd.setGui(new SwingUserConsent(new SwingDialogWrapper(findParentFrame())));
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
                GetRecognitionTree client = (GetRecognitionTree) WSClassLoader.getClientService(RecognitionProperties.getServiceName(), RecognitionProperties.getServiceAddr());
                recognition = new CardRecognition(ifd, ctx, client);
            } catch (Exception ex) {
                _logger.logp(Level.SEVERE, this.getClass().getName(), "init()", ex.getMessage(), ex);
                recognition = null;
                initialized = false;
            }
        } else {
            recognition = null;
        }
        paos = new PAOS(endpointUrl);
        env.addTransport("0", paos);
        em = new EventManager(recognition, env, ctx);
        env.setEventManager(em);
        sal = new MicroSAL(env);
        em.registerAllEvents(sal);
        jsec = new JSEventCallback(this);
        em.registerAllEvents(jsec);
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
