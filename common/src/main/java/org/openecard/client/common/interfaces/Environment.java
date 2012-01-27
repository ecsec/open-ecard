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

package org.openecard.client.common.interfaces;

import org.openecard.ws.IFD;
import org.openecard.ws.Management;
import org.openecard.ws.SAL;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public interface Environment {

    public void setIFD(IFD ifd);
    public IFD getIFD();

    public void setSAL(SAL sal);
    public SAL getSAL();

    public void setEventManager(EventManager manager);
    public EventManager getEventManager();

    public void setDispatcher(Dispatcher dispatcher);
    public Dispatcher getDispatcher();

    public void setGenericComponent(String id, Object component);
    public Object getGenericComponent(String id);
    
    public void setManagement(Management management);
    public Management getManagement();

}
