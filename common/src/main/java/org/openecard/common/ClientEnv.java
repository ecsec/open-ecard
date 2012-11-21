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

package org.openecard.common;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.openecard.common.interfaces.Dispatchable;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.EventManager;
import org.openecard.ws.IFD;
import org.openecard.ws.Management;
import org.openecard.ws.SAL;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class ClientEnv implements Environment {

    private IFD ifd;
    private SAL sal;
    private EventManager manager;
    private Dispatcher dispatcher;
    private Map<String, Object> genericComponents;
    private Management management;

    public ClientEnv() {
	genericComponents = new ConcurrentSkipListMap<String, Object>();
    }


    @Override
    public void setIFD(IFD ifd) {
	this.ifd = ifd;
    }

    @Override
    @Dispatchable
    public IFD getIFD() {
	return ifd;
    }

    @Override
    public void setEventManager(EventManager manager) {
	this.manager = manager;
    }

    @Override
    public EventManager getEventManager() {
	return manager;
    }

    @Override
    public void setSAL(SAL sal) {
	this.sal = sal;
    }

    @Override
    @Dispatchable
    public SAL getSAL() {
	return sal;
    }

    @Override
    public void setDispatcher(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public Dispatcher getDispatcher() {
	return dispatcher;
    }

    @Override
    public void setGenericComponent(String id, Object component) {
	genericComponents.put(id, component);
    }

    @Override
    public Object getGenericComponent(String id) {
	return genericComponents.get(id);
    }

    @Override
    public void setManagement(Management management) {
	this.management = management;
    }

    @Override
    @Dispatchable
    public Management getManagement() {
	return management;
    }

}
