/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

package org.openecard.common.interfaces;

import java.lang.reflect.InvocationTargetException;
import java.util.List;


/**
 * Interface for a webservice method dispatcher.
 * The dispatcher receives an object which is a JAXB type parameter of a webservice method. The dispatcher the looks for
 * a webservice with a suitable method and executes it.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public interface Dispatcher {

    /**
     * Invokes the service which is responsible for messages of the type of the given request object.
     *
     * @param request Object to dispatch to related service.
     * @return The result of the method invocation.
     * @throws DispatcherException In case an error happens in the reflections part of the dispatcher.
     * @throws InvocationTargetException In case the dispatched method throws en exception.
     */
    Object deliver(Object request) throws DispatcherException, InvocationTargetException;

    /**
     * Get a list of String with the available services.
     * The format of the name depends on the name space of the service so the service name has either the prefix
     * {@code urn:iso:std:iso-iec:24727:tech:schema} or {@code http://www.bsi.bund.de/ecard/api/1.0}. A valid example
     * is for instance <br>
     * {@code http://www.bsi.bund.de/ecard/api/1.0#InitializeFramework} or <br>
     * {@code urn:iso:std:iso-iec:24727:tech:schema:DIDAuthenticate}.
     *
     * @return A list of Strings representing the available services of the dispatcher.
     */
    List<String> getServiceList();

    /**
     * Get a Dispatcher which provides a implementation specific range of services.
     * This method is primarily meant to provide a Dispatcher with restricted access to functions which should not be
     * invoked by external communication partners. For instance in the PAOS message exchange of the TR-03112 add-on we
     * do not want to provide access to the CardApplicationPath method which would be enable the remote party to view all
     * available terminals and cards.
     *
     * @return A Dispatcher which provides implementation specific range of services.
     */
    Dispatcher getFilter();

}
