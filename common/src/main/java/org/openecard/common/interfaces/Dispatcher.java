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

package org.openecard.common.interfaces;

import java.lang.reflect.InvocationTargetException;


/**
 * Interface for a webservice method dispatcher.
 * The dispatcher receives an object which is a JAXB type parameter of a webservice method. The dispatcher the looks for
 * a webservice with a suitable method and executes it.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
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

}
