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

package org.openecard.transport.dispatcher;

import org.openecard.common.interfaces.Dispatchable;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.EventManager;
import org.openecard.ws.IFD;
import org.openecard.ws.Management;
import org.openecard.ws.SAL;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TestEnv1 implements Environment {

    private IFD ifd;

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

    @Override
    public void setManagement(Management m) {
	throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public Management getManagement() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
