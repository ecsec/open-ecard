/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
 ************************************************************************** */
package org.openecard.android.activation;

import android.content.Context;
import org.openecard.common.util.SysUtils;
import org.openecard.mobile.activation.common.CommonActivationUtils;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.scio.AndroidNFCFactory;
import org.openecard.scio.CachingTerminalFactoryBuilder;
import org.openecard.ws.jaxb.JAXBMarshaller;

/**
 *
 * @author Neil Crossley
 */
public class OpeneCard {

    static {
	// define that this system is Android
	SysUtils.setIsAndroid();
    }

    private final CommonActivationUtils utils;
    private final CachingTerminalFactoryBuilder<AndroidNFCFactory> builder;

    OpeneCard(CommonActivationUtils utils, CachingTerminalFactoryBuilder<AndroidNFCFactory> builder) {
	this.utils = utils;
	this.builder = builder;
    }

    public AndroidContextManager context(Context context) {
	AndroidNfcCapabilities capabilities = AndroidNfcCapabilities.Companion.create(context);

	return new DelegatingAndroidContextManager(this.utils.context(capabilities), this.builder);
    }

    public static OpeneCard createInstance() {

	CachingTerminalFactoryBuilder<AndroidNFCFactory> factory = new CachingTerminalFactoryBuilder<>(() -> new AndroidNFCFactory());

	OpeneCardContextConfig config = new OpeneCardContextConfig(factory, JAXBMarshaller.class.getCanonicalName());
	CommonActivationUtils activationUtils = new CommonActivationUtils(config, new NFCDialogMsgSetter() {
	    @Override
	    public void setText(String msg) {
	    }

	    @Override
	    public boolean isSupported() {
		return false;
	    }

	});
	return new OpeneCard(activationUtils, factory);
    }
}
