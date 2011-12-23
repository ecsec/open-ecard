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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.logging.LogManager;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class AppletWorker extends Thread implements EventCallback {

    private static final Logger _logger = LogManager.getLogger(AppletWorker.class.getName());

    private ECardApplet applet;
    private String selection;
    private boolean eventOccurred;

    public AppletWorker(ECardApplet applet) {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "AppletWorker(ECardApplet applet)", applet);
        }
        this.applet = applet;
        eventOccurred = false;
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "AppletWorker(ECardApplet applet)");
        }
    }

    public void startPAOS(String ifdName) {
        synchronized(this) {
            selection = ifdName;
            this.notify();
        }
    }

    @Override
    public void run() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "run()");
        }
        List<ConnectionHandleType> cHandles = null;

        if (applet.getSpBehavior().equals(ECardApplet.INSTANT)) {
            cHandles = getConnectionHandles();
        }

        if (applet.getSpBehavior().equals(ECardApplet.WAIT)) {
            if (!eventOccurred) {
                waitForInput();
            }
            cHandles = getConnectionHandles();
        }

        if (applet.getSpBehavior().equals(ECardApplet.CLICK)) {
            waitForInput();
            if (selection != null) {
                cHandles = new ArrayList<ConnectionHandleType>(1);
                ConnectionHandleType cHandle = getConnectionHandle(selection);
                cHandles.add(cHandle);
            } else {
                cHandles = getConnectionHandles();
            }
        }

        StartPAOS sp = new StartPAOS();
        sp.getConnectionHandle().addAll(cHandles);
        sp.setSessionIdentifier(applet.getSessionId());

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

    private void waitForInput() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException ex) {
                // Oh oh,...
            }
        }
    }

    private ConnectionHandleType getConnectionHandle(String ifdName) {
        List<ConnectionHandleType> cHandles = getConnectionHandles();
        for (ConnectionHandleType cHandle : cHandles) {
            if (ifdName.equals(cHandle.getIFDName())) {
                return cHandle;
            }
        }
        return null;
    }

    private List<ConnectionHandleType> getConnectionHandles() {
        return applet.getTinySAL().getConnectionHandles();
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
        if (eventType.equals(EventType.CARD_INSERTED) || eventType.equals(EventType.CARD_RECOGNIZED)) {
            applet.getEnv().getEventManager().unregister(this);
            synchronized(this) {
                eventOccurred = true;
            }
            startPAOS(null);
        }
    }

}
