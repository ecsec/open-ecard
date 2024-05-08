/****************************************************************************
 * Copyright (C) 2019-2024 ecsec GmbH.
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
 */

package org.openecard.android.activation

import android.content.Context
import org.openecard.common.util.SysUtils
import org.openecard.mobile.activation.common.CommonActivationUtils
import org.openecard.mobile.activation.common.NFCDialogMsgSetter
import org.openecard.mobile.system.OpeneCardContextConfig
import org.openecard.scio.AndroidNFCFactory
import org.openecard.scio.CachingTerminalFactoryBuilder
import org.openecard.ws.jaxb.JAXBMarshaller
import org.openecard.ws.common.GenericInstanceProvider


/**
 *
 * @author Neil Crossley
 */
class OpeneCard internal constructor(
	private val utils: CommonActivationUtils,
	builder: CachingTerminalFactoryBuilder<AndroidNFCFactory?>,
) {

	private val builder: CachingTerminalFactoryBuilder<AndroidNFCFactory?> = builder

	fun context(context: Context?): AndroidContextManager {
		val capabilities: AndroidNfcCapabilities = AndroidNfcCapabilities.Companion.create(context)
		return DelegatingAndroidContextManager(utils.context(capabilities), this.builder)
	}

	companion object {
		init {
			// define that this system is Android
			SysUtils.setIsAndroid()
		}

		fun createInstance(): OpeneCard {
			val androidNfcFactory : GenericInstanceProvider<AndroidNFCFactory?> = object : GenericInstanceProvider<AndroidNFCFactory?> {
				override val instance = AndroidNFCFactory()
			}
			val factory: CachingTerminalFactoryBuilder<AndroidNFCFactory?> = CachingTerminalFactoryBuilder<AndroidNFCFactory?>(androidNfcFactory)

			val config = OpeneCardContextConfig(factory, JAXBMarshaller::class.java.getCanonicalName())
			val activationUtils = CommonActivationUtils(config, object : NFCDialogMsgSetter {
				override fun setText(msg: String) { }

				override fun isSupported(): Boolean {
					return false
				}
			})
			return OpeneCard(activationUtils, factory)
		}
	}
}
