/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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

package org.openecard.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import org.openecard.addon.ActionInitializationException;
import org.openecard.addon.Context;
import org.openecard.addon.sal.SALProtocolBaseImpl;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.interfaces.ObjectSchemaValidator;
import org.openecard.common.util.FuturePromise;
import org.openecard.common.util.MarshallerSchemaValidator;
import org.openecard.common.util.Promise;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


/**
 * Implementation of the EACProtocol using only DIDAuthenticate messages.
 * This class also contains lookup keys for {@link DynamicContext}.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class EACProtocol extends SALProtocolBaseImpl {

    private static final Logger LOG = LoggerFactory.getLogger(EACProtocol.class);

    private static final String PREFIX = "org.openecard.tr03112.eac.";

    public static final String EAC_DATA = PREFIX + "eac_data";
    public static final String PIN_STATUS = PREFIX + "pin_status";
    public static final String IS_NATIVE_PACE = PREFIX + "is_native_pace";
    public static final String PACE_MARKER = PREFIX + "pace_marker";
    public static final String PACE_EXCEPTION = PREFIX + "pace_successful";
    public static final String SLOT_HANDLE = PREFIX + "slot_handle";
    public static final String DISPATCHER = PREFIX + "dispatcher";
    public static final String SCHEMA_VALIDATOR = PREFIX + "schema_validator";
    public static final String AUTHENTICATION_DONE = PREFIX + "authentication_done";


    @Override
    public void init(Context ctx) throws ActionInitializationException {
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	dynCtx.putPromise(SCHEMA_VALIDATOR, new FuturePromise<ObjectSchemaValidator>(() -> {
	    boolean noValid = Boolean.valueOf(OpenecardProperties.getProperty("legacy.ignore_ns"));
	    if (! noValid) {
		try {
		    return MarshallerSchemaValidator.load(DIDAuthenticate.class, "ISO24727-Protocols.xsd");
		} catch (SAXException | WSMarshallerException ex) {
		    LOG.warn("No Schema Validator available, skipping schema validation.", ex);
		}
	    }
	    // always valid
	    return (obj) -> true;
	}));

	addOrderStep(new PACEStep(ctx.getDispatcher(), ctx.getUserConsent(), ctx.getEventDispatcher()));
	addOrderStep(new TerminalAuthenticationStep(ctx.getDispatcher()));
	addOrderStep(new ChipAuthenticationStep(ctx.getDispatcher()));
    }

    @Override
    public void destroy() {
	LOG.debug("Destroying EAC protocol instance.");
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	Thread guiThread = (Thread) dynCtx.get(TR03112Keys.OPEN_USER_CONSENT_THREAD);
	if (guiThread != null) {
	    // wait for gui to finish
	    try {
		LOG.debug("Waiting for EAC GUI to terminate.");
		guiThread.join();
		LOG.debug("EAC GUI terminated.");
	    } catch (InterruptedException ex) {
		// gui thread has its own handling of the shutdown, so interrupt thread and wait
		LOG.debug("Triggering hard shutdown of EAC GUI.");
		guiThread.interrupt();
		// wait again until the GUI is actually gone
		try {
		    guiThread.join();
		} catch (InterruptedException ex2) {
		    // ignore as we continue anyway
		}
	    }
	}
    }

    @Override
    public boolean isFinished() {
	LOG.debug("Checking if EAC protocol is finished.");
	boolean finished = super.isFinished();
	if (! finished) {
	    DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    Promise p = ctx.getPromise(EACProtocol.AUTHENTICATION_DONE);
	    if (p.isDelivered()) {
		LOG.debug("EAC AUTHENTICATION_DONE promise is delivered.");
		finished = true;
		try {
		    boolean failed = ! (boolean) p.deref();
		    if (failed) {
			LOG.debug("EAC AUTHENTICATION_FAILED promise is delivered.");
		    } else {
			LOG.debug("EAC AUTHENTICATION_DONE promise is delivered.");
		    }
		} catch (InterruptedException ex) {
		    // error would mean don't use the value, so this is ok to ignore
		}
	    }
	}
	LOG.debug("EAC authentication finished={}.", finished);
	return finished;
    }

}
