/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.binding.tctoken;

import org.openecard.common.DynamicContext;
import org.openecard.common.util.Promise;
import org.openecard.httpcore.ResourceContextLoader;
import org.openecard.httpcore.cookies.CookieManager;


/**
 * Resourceloader with specific changes for use in accordance to TR-03112.
 *
 * @author Tobias Wich
 */
public class TrResourceContextLoader extends ResourceContextLoader {

    @Override
    protected CookieManager getCookieManager() {
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	CookieManager cManager = (CookieManager) dynCtx.get(TR03112Keys.COOKIE_MANAGER);
	return cManager;
    }

    @Override
    protected boolean isPKIXVerify() {
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	Promise<Object> cardTypeP = dynCtx.getPromise(TR03112Keys.ACTIVATION_CARD_TYPE);
	Object cardType = cardTypeP.derefNonblocking();
	// verify when the value is not set or when no nPA is requested
	if (cardType != null && ! "http://bsi.bund.de/cif/npa.xml".equals(cardType)) {
	    return true;
	} else {
	    return false;
	}
    }

}
