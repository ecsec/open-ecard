/*
 * Copyright 2012 Tobias Wich ecsec GmbH
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

package org.openecard.client.transport.dispatcher;

import org.openecard.client.common.interfaces.Dispatchable;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventManager;
import org.openecard.ws.IFD;
import org.openecard.ws.SAL;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TestEnv implements Environment {

    private IFD ifd;

    @Override
    public void setIFD(IFD ifd) {
	this.ifd = ifd;
    }

    @Override
    @Dispatchable(interfaceClass=IFD.class)
    public IFD getIFD() {
	return ifd;
    }

    @Override
    public void setSAL(SAL sal) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SAL getSAL() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setEventManager(EventManager manager) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EventManager getEventManager() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDispatcher(Dispatcher dispatcher) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Dispatcher getDispatcher() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setGenericComponent(String id, Object component) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getGenericComponent(String id) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
