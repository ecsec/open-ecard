/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.binding.tctoken

import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants.NPA_CARD_TYPE
import org.openecard.httpcore.ResourceContextLoader
import org.openecard.httpcore.cookies.CookieManager

/**
 * Resourceloader with specific changes for use in accordance to TR-03112.
 *
 * @author Tobias Wich
 */
class TrResourceContextLoader : ResourceContextLoader() {
	override var cookieManager: CookieManager?
		get() {
			val dynCtx =
				DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
			val cManager =
				dynCtx.get(TR03112Keys.COOKIE_MANAGER) as CookieManager?
			return cManager
		}
		set(value) {
			val dynCtx =
				DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
			dynCtx.put(TR03112Keys.COOKIE_MANAGER, value)
		}

	override val isPKIXVerify: Boolean
		get() {
			val dynCtx =
				DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
			val cardTypeP =
				dynCtx.getPromise(TR03112Keys.ACTIVATION_CARD_TYPE)
			val cardType = cardTypeP.derefNonblocking()
			// verify when the value is not set or when no nPA is requested
			return cardType != null && NPA_CARD_TYPE != cardType
		}
}
