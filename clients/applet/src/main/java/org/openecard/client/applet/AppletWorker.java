package org.openecard.client.applet;

import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.logging.LogManager;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
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
        List<ConnectionHandleType> cHandles = applet.getTinySAL().getConnectionHandles();

        // TODO: use event system instead of polling mechanism
        if (applet.waitForCard()) {
            boolean cardPresent = false;
            while (!cardPresent) {
                cHandles = applet.getTinySAL().getConnectionHandles();
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

        StartPAOS sp = new StartPAOS();
        sp.getConnectionHandle().addAll(cHandles);
        sp.setSessionIdentifier(applet.getSessionId());
        Object message = sp;
        try {
            Object result = applet.getPAOS().sendStartPAOS(sp);

            String redirectUrl = applet.getRedirectUrl();
            if (redirectUrl != null) {
                try {
                    applet.getAppletContext().showDocument(new URL(redirectUrl), "_top");
                } catch (MalformedURLException ex) {
                    if (_logger.isLoggable(Level.WARNING)) {
                        _logger.logp(Level.WARNING, this.getClass().getName(), "run()", ex.getMessage(), ex);
                    }
                }
                return;
            } else {
                if (_logger.isLoggable(Level.WARNING)) {
                    _logger.logp(Level.WARNING, this.getClass().getName(), "run()", "Unknown response type.", result);
                }
                return;
            }
        } catch (Exception ex) {
            _logger.logp(Level.SEVERE, AppletWorker.class.getName(), "run()", "Failure occured while sending or receiving PAOS messages from endpoint " + applet.getEndpointUrl() + ".", ex);
        }

        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "run()");
        }
    }
}
