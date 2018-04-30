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

import org.openecard.common.sal.state.CardStateMap;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.List;
import org.openecard.common.interfaces.CIFProvider;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.mdlw.sal.config.MiddlewareConfig;
import org.openecard.mdlw.sal.config.MiddlewareConfigLoader;
import org.openecard.mdlw.sal.MwSlot;
import org.openecard.mdlw.sal.MwToken;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.openecard.mdlw.sal.exceptions.TokenException;


/**
 *
 * @author Jan Mannsbart
 */
public class MwStateCallback {

    private static final Logger LOG = LoggerFactory.getLogger(MwStateCallback.class);

    private final Environment env;
    private final CardStateMap states;
    private final List<MiddlewareConfig> mwConfigs;

    public MwStateCallback(Environment env, CardStateMap cardState, MiddlewareConfigLoader mwConfigLoader) {
        this.env = env;
        this.states = cardState;
        this.mwConfigs = mwConfigLoader.getMiddlewareConfigs();
    }

    public boolean addEntry(MwEventObject o) throws TokenException {
	try {
	    ConnectionHandleType handle = o.getHandle();
	    MwSlot slot = o.getMwSlot();

	    MwToken token = slot.getTokenInfo();
	    String cardType = null;
	    String type = String.format("%s_%s", token.getManufacturerID(), token.getModel());
	    for (MiddlewareConfig mwConfig : mwConfigs) {
		cardType = mwConfig.mapMiddlewareType(type);
		if (cardType != null) {
		    break;
		}
	    }
	    CardInfoType cif = null;
	    LOG.debug("Determined cardType={} for middleware name={}.", cardType, type);
	    if (cardType != null) {
		LOG.debug("Requesting CIF from CIF provider.");
		CIFProvider cp = env.getCIFProvider();
		cif = cp.getCardInfo(handle, cardType);
	    }
	    if (cif == null) {
		LOG.warn("Unknown card recognized by Middleware.");
		return false;
	    }

	    // create new entry in card states
	    CardStateEntry entry = new CardStateEntry(handle, cif, null);
	    states.addEntry(entry);
	    return true;
	} catch (CryptokiException ex) {
	    LOG.info("Cryptoki Token invalid.", ex);
	    throw new TokenException("Cryptoki Token invalid.", ex.getErrorCode());
	} catch (RuntimeException ex) {
	    LOG.error("Error in CIF augmentation process.", ex);
	    throw new TokenException("Error in CIF augmentation process.", CryptokiLibrary.CKR_TOKEN_NOT_RECOGNIZED);
	}
    }

    public void removeEntry(MwEventObject o) {
	LOG.info("Remove card");
	ConnectionHandleType handle = o.getHandle();
	states.removeEntry(handle);
    }

}
