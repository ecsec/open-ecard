/*
 * Copyright 2012 Johannes Schmoelz, Tobias Wich ecsec GmbH
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

package org.openecard.client.common;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.openecard.client.common.interfaces.Dispatchable;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventManager;
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
    public Management getManagement() {
	return management;
    }
    
}
