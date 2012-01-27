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

package org.openecard.client.ws;

import org.openecard.client.common.GenericFactory;
import org.openecard.client.common.GenericFactoryException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WSMarshallerFactory {

    private GenericFactory<WSMarshaller> factory;

    private WSMarshallerFactory() throws WSMarshallerException {
        try {
            factory = new GenericFactory<WSMarshaller>(WsdefProperties.properties(), "org.openecard.client.ws.marshaller.impl");
        } catch (GenericFactoryException ex) {
            throw new WSMarshallerException(ex);
        }
    }


    private static WSMarshallerFactory inst = null;

    public static WSMarshaller createInstance() throws WSMarshallerException {
        if (inst == null) {
            inst = new WSMarshallerFactory();
        }

        try {
            return inst.factory.getInstance();
        } catch (GenericFactoryException ex) {
            throw new WSMarshallerException(ex);
        }
    }

}
