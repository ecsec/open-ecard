/****************************************************************************
 * Copyright (C) 2012-2015 HS Coburg.
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
import java.util.concurrent.Callable;
import org.openecard.addon.ActionInitializationException;
import org.openecard.addon.Context;
import org.openecard.addon.sal.SALProtocolBaseImpl;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.interfaces.ObjectSchemaValidator;
import org.openecard.common.interfaces.ObjectValidatorException;
import org.openecard.common.util.FuturePromise;
import org.openecard.common.util.JAXBSchemaValidator;
import org.openecard.common.util.Promise;


/**
 * Implementation of the EACProtocol using only DIDAuthenticate messages.
 * This class also contains lookup keys for {@link DynamicContext}.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class EACProtocol extends SALProtocolBaseImpl {

    private static final String PREFIX = "org.openecard.tr03112.eac.";

    public static final String EAC_DATA = PREFIX + "eac_data";
    public static final String PIN_STATUS_BYTES = PREFIX + "pin_status_bytes";
    public static final String IS_NATIVE_PACE = PREFIX + "is_native_pace";
    public static final String PACE_MARKER = PREFIX + "pace_marker";
    public static final String PACE_SUCCESSFUL = PREFIX + "pace_successful";
    public static final String GUI_RESULT = PREFIX + "gui_result";
    public static final String SLOT_HANDLE = PREFIX + "slot_handle";
    public static final String DISPATCHER = PREFIX + "dispatcher";
    public static final String SCHEMA_VALIDATOR = PREFIX + "schema_validator";
    public static final String AUTHENTICATION_DONE = PREFIX + "authentication_done";
    public static final String AUTHENTICATION_FAILED = PREFIX + "authentication_failed";


    @Override
    public void init(Context ctx) throws ActionInitializationException {
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	dynCtx.putPromise(SCHEMA_VALIDATOR, new FuturePromise<>(new Callable<ObjectSchemaValidator>() {
	    @Override
	    public ObjectSchemaValidator call() throws Exception {
		boolean noValid = Boolean.valueOf(OpenecardProperties.getProperty("legacy.ignore_ns"));
		ObjectSchemaValidator v;
		if (! noValid) {
		    v = JAXBSchemaValidator.load(DIDAuthenticate.class, "ISO24727-Protocols.xsd");
		} else {
		    // always valid
		    v = new ObjectSchemaValidator() {
			@Override
			public boolean validateObject(Object obj) throws ObjectValidatorException {
			    return true;
			}
		    };
		}
		return v;
	    }
	}));

	addOrderStep(new PACEStep(ctx.getDispatcher(), ctx.getUserConsent(), ctx.getEventManager()));
	addOrderStep(new TerminalAuthenticationStep(ctx.getDispatcher()));
	addOrderStep(new ChipAuthenticationStep(ctx.getDispatcher()));
    }

    @Override
    public void destroy() {
	// nothing to see here ... move along
    }

    @Override
    public boolean isFinished() {
	boolean finished = super.isFinished();
	if (! finished) {
	    DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    Promise p = ctx.getPromise(EACProtocol.AUTHENTICATION_DONE);
	    if (p.isDelivered()) {
		try {
		    finished = (boolean) p.deref();
		} catch (InterruptedException ex) {
		    // error would mean don't use the value, so this is ok to ignore
		}
	    }
	    Promise p2 = ctx.getPromise(EACProtocol.AUTHENTICATION_FAILED);
	    if (p2.isDelivered()) {
		try {
		    finished = (boolean) p2.deref();
		} catch (InterruptedException ex) {
		    // error would mean don't use the value, so this is ok to ignore
		}
	    }
	}
	return finished;
    }

}
