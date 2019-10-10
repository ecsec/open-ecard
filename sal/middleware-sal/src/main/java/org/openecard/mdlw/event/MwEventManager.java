/****************************************************************************
 * Copyright (C) 2016-2018 ecsec GmbH.
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

package org.openecard.mdlw.event;

import java.util.concurrent.FutureTask;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.common.util.ValueGenerators;
import org.openecard.mdlw.sal.MiddlewareSAL;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class of the event system.
 * Use this to create and operate an event manager.
 *
 * @author René Lottes
 * @author Tobias Wich
 */
public class MwEventManager {

    private static final Logger LOG = LoggerFactory.getLogger(MwEventManager.class);

    private final MiddlewareSAL mwSAL;
    private final Environment env;

    private final String sessionId;
    private final HandlerBuilder builder;

    private FutureTask<Void> watcher;


    public MwEventManager(Environment env, MiddlewareSAL mwSAL, byte[] contextHandle) {
	this.env = env;
	this.mwSAL = mwSAL;

	this.sessionId = ValueGenerators.genBase64Session();
	this.builder = HandlerBuilder.create()
		.setContextHandle(contextHandle)
		.setSessionId(sessionId);
    }

    public void initialize() throws InitializationException {
	// start watcher thread
	try {
	    DatatypeFactory dataFactory = DatatypeFactory.newInstance();
	    MwEventRunner runner = new MwEventRunner(env, builder, dataFactory, mwSAL.getMwModule());
	    runner.initRunner();
	    watcher = new FutureTask<>(runner, null);
	    Thread t = new Thread(watcher, "MwEventManager");
	    t.start();
	} catch (CryptokiException ex) {
	    LOG.error("Failed to initialize middleware event runner.", ex);
	    throw new InitializationException("Failed to request initial status from middleware.", ex.getErrorCode());
	} catch (DatatypeConfigurationException ex) {
	    throw new UnsupportedOperationException("Datatype factory not supported.", ex);
	}
    }

    public void terminate() {
	watcher.cancel(true);
    }

}
