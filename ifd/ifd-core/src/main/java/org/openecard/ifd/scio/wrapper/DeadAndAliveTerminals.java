/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.ifd.scio.wrapper;

import org.openecard.common.ifd.scio.SCIOTerminals;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import org.openecard.common.GenericFactoryException;
import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.util.ExceptionUtils;
import org.openecard.ifd.scio.IFDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Self healing CardTerminals implementation.
 * <p>When loading the IFDTerminalFactory and pcscd is not running, which is the case on OS X when no terminal is
 * connected, the factory is not working and will never do so. In that case it must be reloaded when a terminal is
 * connected. Unfortunately this can not be detected. In order to save the using context, in our case the IFD stack,
 * from taking care of reinitializing the factory, this class enables transparent access to the terminals with periodic
 * checks to reload the factory.</p>
 * <p>This behaviour can be best explained with the analogy of Schroedingers well known cat. After creating an instance
 * of this class, the terminals wrapped by it can be tought of as either dead (stubbed) or alive (working). By using the
 * functions {@link #list()} and {@link #waitForChange(long)} it can not be differentiated whether the implementation is
 * dead or alive. Only by "opening the box" through the {@link #isDead()} and {@link #isAlive()} functions, the state of
 * the implementation can be observed. Similar to as it is the case wih quantum mechanics, the observer influences the
 * state when observing the status. That means calls to these functions trigger the update of the factory's state.</p>
 * <p>Obviously PCSC on OS X only works when considering the laws of quantum mechanics,</p>
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class DeadAndAliveTerminals extends SCIOTerminals {

    private static final Logger logger = LoggerFactory.getLogger(DeadAndAliveTerminals.class);

    private static final long WAIT_DELTA = (long) (0.5 * 1000);

    private boolean error;
    private long lastTry;
    private SCIOTerminals terminals;

    /**
     * Creates an instance of the DeadAndAliveTerminals.
     *
     * @throws IFDException Thrown in case an unrecoverable error occured while loading the factory.
     */
    public DeadAndAliveTerminals() throws IFDException {
	reloadTerminals();
    }

    private void reloadTerminals() throws IFDException {
	lastTry = System.currentTimeMillis();
	try {
	    // try to load the "alive" terminals
	    TerminalFactory f = IFDTerminalFactory.getInstance();
	    terminals = f.terminals();
	    error = false;
	} catch (IFDException ex) {
	    // check if it is really a SCARD_E_NO_SERVICE error, when not notify the user as this is a real error
	    NoSuchAlgorithmException destEx = ExceptionUtils.matchPath(ex, NoSuchAlgorithmException.class,
		    InvocationTargetException.class, GenericFactoryException.class);
	    // TODO: i can not access the PCSC code, as the class is part of the abstracted sun classes
	    if (destEx != null && destEx.getCause() != null &&
		destEx.getCause().getClass().getName().endsWith(".PCSCException")) {
		error = true;
	    } else {
		// ok this is serious
		throw ex;
	    }
	}
    }


    /**
     * Checks whether the implementation is dead, meaning it is stubbed because the real implementation fails to load.
     * This function tries to reload the implementation when it is errornous. In order to prevent producing too much
     * load, the function only performs the check again after a short period of time since the last time.
     *
     * @return True if the implementation is dead, false otherwise.
     */
    public synchronized boolean isDead() {
	tryReloadWhenError();
	return error;
    }
    /**
     * The inverse of {@link #isAlive()}.
     *
     * @return True if the implementation is alive, false otherwise.
     */
    public synchronized boolean isAlive() {
	return ! isDead();
    }

    private synchronized void tryReloadWhenError() {
	// try to reload only if implementaion is errornous and we waited long enough
	long now = System.currentTimeMillis();
	if (error && (now - lastTry) > WAIT_DELTA) {
	    try {
		reloadTerminals();
	    } catch (IFDException ex) {
		logger.error("The TerminalFactory has a serious problem.", ex);
	    }
	}
    }

    @Override
    public synchronized List<SCIOTerminal> list(State state) throws SCIOException {
	if (isDead()) {
	    return Collections.emptyList();
	} else {
	    return terminals.list(state);
	}
    }

    @Override
    public boolean waitForChange(long timeout) throws SCIOException {
	if (timeout < 0) {
	    throw new IllegalArgumentException("Timeout is negative.");
	} else if (timeout == 0) {
	    // fix timeout value, so the handcrufted wait works as expected
	    timeout = Long.MAX_VALUE;
	}
	long start = System.currentTimeMillis();
	// as long as the terminal is dead we have to take care of the wait for ourselves
	// wait for a small time frame and check if the terminal is alive again
	while (isDead() && (System.currentTimeMillis() - start) < timeout) {
	    try {
		Thread.sleep(WAIT_DELTA);
	    } catch (InterruptedException ex) {
		// TODO: how can we break execution completely here?
	    }
	}
	// either return false or perform the wait with the "alive" terminals
	synchronized (this) {
	    if (isDead()) {
		return false;
	    } else {
		return terminals.waitForChange(timeout);
	    }
	}
    }

}
