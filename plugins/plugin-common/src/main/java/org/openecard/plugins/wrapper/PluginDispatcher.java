/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.plugins.wrapper;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;


/**
 * Wrapper for implementations of the {@code Dispatcher}-interface that should be used by plugins. Each method-call is
 * wrapped by a doPrivileged-call to give the plugins the possibility to call our code that performs actions, which they
 * would otherwise not have the permissions for.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PluginDispatcher implements Dispatcher {

    private final Dispatcher mDispatcher;

    /**
     * Creates a new PluginDispatcher wrapping the given Dispatcher. 
     * 
     * @param messageDispatcher The Dispatcher which is wrapped and to which calls are forwarded to.
     */
    public PluginDispatcher(Dispatcher messageDispatcher) {
	this.mDispatcher = messageDispatcher;
    }

    @Override
    public Object deliver(final Object request) throws DispatcherException, InvocationTargetException {
	try {
	    // wrap deliver-method with doPrivileged
	    Object response = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
		public Object run() throws DispatcherException, InvocationTargetException {
		    return mDispatcher.deliver(request);
		}
	    });
	    return response;
	} catch (PrivilegedActionException e) {
	    if (e.getException() instanceof DispatcherException) {
		throw (DispatcherException) e.getException();
	    } else {
		throw (InvocationTargetException) e.getException();
	    }
	}
    }

}
