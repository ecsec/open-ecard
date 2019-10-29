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
import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.common.CommonActivationUtils;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.scio.AndroidNFCFactory;
import org.openecard.ws.android.AndroidMarshaller;

/**
 *
 * @author Neil Crossley
 */
public class OpeneCard {


    static {
	Provider provider = new BouncyCastleProvider();
	try {
	    Security.removeProvider(provider.getName());
	} catch (Exception e) {
	}
	Security.addProvider(provider);
    }

    private final CommonActivationUtils utils;
    private ContextManager context;

    OpeneCard(CommonActivationUtils utils) {
	this.utils = utils;
    }

    public ContextManager context(Context context) {
	AndroidNfcCapabilities capabilities = AndroidNfcCapabilities.create(context);
	this.context = this.utils.context(capabilities);
	return this.context;
    }

    public static OpeneCard createInstance() {

	OpeneCardContextConfig config = new OpeneCardContextConfig(AndroidNFCFactory.class.getCanonicalName(), AndroidMarshaller.class.getCanonicalName());
	CommonActivationUtils activationUtils = new CommonActivationUtils(config, new NFCDialogMsgSetter() {
	    @Override
	    public void setText(String msg) {
	    }

	    @Override
	    public boolean isSupported() {
		return false;
	    }

	});
	return new OpeneCard(activationUtils);
    }
}
