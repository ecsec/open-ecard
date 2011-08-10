package de.ecsec.ecard.client;

import de.ecsec.core.common.logging.LogManager;
import de.ecsec.core.environment.ClientEnv;
import iso.std.iso_iec._24727.tech.schema.BeginTransaction;
import iso.std.iso_iec._24727.tech.schema.Cancel;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ControlIFD;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.EndTransaction;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ModifyVerificationData;
import iso.std.iso_iec._24727.tech.schema.Output;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.VerifyUser;
import iso.std.iso_iec._24727.tech.schema.Wait;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class AppletWorker implements Runnable {

    private static final Logger _logger = LogManager.getLogger(AppletWorker.class.getName());
    private ECardApplet applet;
    private ClientEnv env;
    private boolean isRunning;

    public AppletWorker(ECardApplet applet) {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "AppletWorker(ECardApplet applet)", applet);
        }
        this.applet = applet;
        env = applet.getEnv();
        isRunning = true;
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "AppletWorker(ECardApplet applet)");
        }
    }

    public void interrupt() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "interrupt()");
        }
        isRunning = false;
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "interrupt()");
        }
    }

    @Override
    public void run() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "run()");
        }
        List<ConnectionHandleType> cHandles = applet.getMicroSAL().getConnectionHandles();

        // TODO: use event system instead of polling mechanism
        if (applet.waitForCard()) {
            boolean cardPresent = false;
            while (!cardPresent) {
                cHandles = applet.getMicroSAL().getConnectionHandles();
                for (ConnectionHandleType cHandle : cHandles) {
                    if (cHandle.getRecognitionInfo() != null) {
                        cardPresent = true;
                        break;
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    // do nothing here...
                }
            }
        }

        System.out.println("AppletWorker :: create StartPAOS");
        StartPAOS sp = new StartPAOS();
        sp.getConnectionHandle().addAll(cHandles);
        sp.setSessionIdentifier(applet.getSessionId());
        Object message = sp;
        Object result = null;
        while (isRunning) {
            result = env.getTransport("0").send(message);
            if (result instanceof EstablishContext) {
                EstablishContext request = (EstablishContext) result;
                message = env.getIFD().establishContext(request);
            } else if (result instanceof ReleaseContext) {
                ReleaseContext request = (ReleaseContext) result;
                message = env.getIFD().releaseContext(request);
            } else if (result instanceof ListIFDs) {
                ListIFDs request = (ListIFDs) result;
                message = env.getIFD().listIFDs(request);
            } else if (result instanceof GetIFDCapabilities) {
                GetIFDCapabilities request = (GetIFDCapabilities) result;
                message = env.getIFD().getIFDCapabilities(request);
            } else if (result instanceof GetStatus) {
                GetStatus request = (GetStatus) result;
                message = env.getIFD().getStatus(request);
            } else if (result instanceof Wait) {
                Wait request = (Wait) result;
                message = env.getIFD().wait(request);
            } else if (result instanceof Cancel) {
                Cancel request = (Cancel) result;
                message = env.getIFD().cancel(request);
            } else if (result instanceof ControlIFD) {
                ControlIFD request = (ControlIFD) result;
                message = env.getIFD().controlIFD(request);
            } else if (result instanceof Connect) {
                Connect request = (Connect) result;
                message = env.getIFD().connect(request);
            } else if (result instanceof Disconnect) {
                Disconnect request = (Disconnect) result;
                message = env.getIFD().disconnect(request);
            } else if (result instanceof BeginTransaction) {
                BeginTransaction request = (BeginTransaction) result;
                message = env.getIFD().beginTransaction(request);
            } else if (result instanceof EndTransaction) {
                EndTransaction request = (EndTransaction) result;
                message = env.getIFD().endTransaction(request);
            } else if (result instanceof Transmit) {
                Transmit request = (Transmit) result;
                message = env.getIFD().transmit(request);
            } else if (result instanceof VerifyUser) {
                VerifyUser request = (VerifyUser) result;
                message = env.getIFD().verifyUser(request);
            } else if (result instanceof ModifyVerificationData) {
                ModifyVerificationData request = (ModifyVerificationData) result;
                message = env.getIFD().modifyVerificationData(request);
            } else if (result instanceof Output) {
                Output request = (Output) result;
                message = env.getIFD().output(request);
            } else if (result instanceof StartPAOSResponse) {
                String redirectUrl = applet.getRedirectUrl();
                if (redirectUrl != null) {
                    try {
                        applet.getAppletContext().showDocument(new URL(redirectUrl), "_top");
                    } catch (MalformedURLException ex) {
                        if (_logger.isLoggable(Level.WARNING)) {
                            _logger.logp(Level.WARNING, this.getClass().getName(), "run()", ex.getMessage(), ex);
                        }
                    }
                }
                return;
            } else {
                if (_logger.isLoggable(Level.WARNING)) {
                    _logger.logp(Level.WARNING, this.getClass().getName(), "run()", "Unknown response type.", result);
                }
                return;
            }
        }
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "run()");
        }
    }
}
