/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard Client.
 *
 * GNU General Public License Usage
 *
 * Open eCard Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open eCard Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Other Usage
 *
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ****************************************************************************/

package org.openecard.client.applet;

import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
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
                 // need a slothandle
                 Connect c = new Connect();
                 c.setContextHandle(cHandle.getContextHandle());
                 c.setExclusive(false);
                 c.setIFDName(selection);
                 c.setSlot(new BigInteger("0"));
                 ConnectResponse cr = this.applet.getEnv().getIFD().connect(c);
                 cHandle.setSlotHandle(cr.getSlotHandle());
                 //doesn't work with mtg testserver !? so remove 
                 cHandle.setRecognitionInfo(null);
                 cHandle.setChannelHandle(null);
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

           /* String redirectUrl = applet.getRedirectUrl();
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
            }*/
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
