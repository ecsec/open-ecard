/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.addons.cg.activate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.xml.transform.TransformerException;
import org.openecard.addon.Context;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addons.cg.ex.ChipGatewayUnknownError;
import org.openecard.addons.cg.tctoken.TCToken;
import org.openecard.addons.cg.ex.InvalidRedirectUrlException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.gui.UserConsent;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.addons.cg.ex.ErrorTranslations.*;
import org.openecard.addons.cg.ex.FatalActivationError;
import org.openecard.addons.cg.ex.InvalidTCTokenElement;
import org.openecard.addons.cg.ex.RedirectionBaseError;
import org.openecard.addons.cg.ex.ResultMinor;
import org.openecard.addons.cg.impl.ChipGatewayResponse;
import org.openecard.addons.cg.impl.ChipGatewayTask;
import org.openecard.common.ThreadTerminateException;
import org.openecard.ws.chipgateway.TerminateType;


/**
 * Transport binding agnostic TCToken handler. <br>
 * This handler supports the following transports:
 * <ul>
 * <li>ChipGateway</li>
 * </ul>
 * <p>
 * This handler supports the following security protocols:
 * <ul>
 * <li>ChipGateway</li>
 * </ul>
 *
 * @author Tobias Wich
 */
public class TCTokenHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TCTokenHandler.class);

    private static final AtomicInteger THREAD_NUM = new AtomicInteger(1);

    private final Dispatcher dispatcher;
    private final UserConsent gui;

    /**
     * Creates a TCToken handler instances and initializes it with the given parameters.
     *
     * @param ctx Context containing instances to the core modules.
     */
    public TCTokenHandler(Context ctx) {
	this.dispatcher = ctx.getDispatcher();
	this.gui = ctx.getUserConsent();
    }


    /**
     * Performs the actual ChipGateway procedure.
     * Connects the given card, establishes the HTTP channel and talks to the server. Afterwards disconnects the card.
     *
     * @param token The TCToken containing the connection parameters.
     * @return A TCTokenResponse indicating success or failure.
     * @throws DispatcherException If there was a problem dispatching a request from the server.
     * @throws ChipGatewayException If there was a transport error.
     */
    private ChipGatewayResponse processBinding(@Nonnull TCToken token) throws InvalidTCTokenElement,
            RedirectionBaseError, InvalidRedirectUrlException {
	ChipGatewayResponse response = new ChipGatewayResponse();
        response.setToken(token);

	String binding = token.getBinding();
	switch (binding) {
	    case "http://ws.openecard.org/binding/chipgateway": {
		ChipGatewayTask task = new ChipGatewayTask(dispatcher, token, gui);
		FutureTask<TerminateType> cgTask = new FutureTask<>(task);
		Thread cgThread = new Thread(cgTask, "ChipGateway-" + THREAD_NUM.getAndIncrement());
		cgThread.start();
		// wait for computation to finish
		waitForTask(token, cgTask, cgThread);
		break;
	    }
	    default:
		// unknown binding
		throw new InvalidTCTokenElement(ELEMENT_VALUE_INVALID, "Binding");
	}

	return response;
    }


    /**
     * Activates the client according to the received TCToken.
     *
     * @param token The activation TCToken.
     * @return The response containing the result of the activation process.
     */
    public BindingResult handleNoCardActivate(TCToken token) {
	if (LOG.isDebugEnabled()) {
	    try {
		WSMarshaller m = WSMarshallerFactory.createInstance();
		LOG.debug("TCToken:\n{}", m.doc2str(m.marshal(token)));
	    } catch (TransformerException | WSMarshallerException ex) {
		// it's no use
	    }
	}

	try {
	    // process binding and follow redirect addresses afterwards
	    ChipGatewayResponse response = processBinding(token);
	    // fill in values, so it is usuable by the transport module
	    response.finishResponse();
	    return response;
	} catch (RedirectionBaseError ex) {
	    LOG.error(ex.getMessage(), ex);
            return ex.getBindingResult();
        } catch (FatalActivationError ex) {
	    LOG.error(ex.getMessage(), ex);
            return ex.getBindingResult();
        }
    }

    private void waitForTask(@Nonnull TCToken token, @Nonnull Future<?> task, Thread thread) throws RedirectionBaseError,
            InvalidRedirectUrlException {
	try {
	    task.get();
	} catch (InterruptedException ex) {
	    task.cancel(true);
	    try {
		thread.join();
	    } catch (InterruptedException ignore) {
		// no one cares
	    }
	    LOG.info("ChipGateway protocol task cancelled.", ex);
	    throw new ThreadTerminateException("Waiting for ChipGateway task interrupted.", ex);
	} catch (ExecutionException ex) {
	    LOG.error(ex.getMessage(), ex);
	    // perform conversion of ExecutionException from the Future to the really expected exceptions
	    if (ex.getCause() instanceof RedirectionBaseError) {
		throw (RedirectionBaseError) ex.getCause();
	    } else {
		throw new ChipGatewayUnknownError(token.finalizeErrorAddress(ResultMinor.CLIENT_ERROR),
                        UNKOWN, ex);
	    }
	}
    }

}
