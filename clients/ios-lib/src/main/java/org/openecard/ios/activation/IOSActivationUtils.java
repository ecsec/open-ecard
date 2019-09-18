/****************************************************************************
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
 ***************************************************************************/

package org.openecard.ios.activation;

import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.EacControllerFactory;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.activation.common.CommonActivationUtils;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.robovm.annotations.FrameworkObject;
import org.openecard.scio.IOSNFCFactory;
import org.openecard.ws.jaxb.JAXBMarshaller;

/**
 *
 * @author Neil Crossley
 */
@FrameworkObject(factoryMethod = "createActivationUtils")
public class IOSActivationUtils implements IOSActivationUtilsInterface {

    private final IOSNFCCapabilities capabilities;
    private final CommonActivationUtils utils;

    public IOSActivationUtils() {
	IOSNFCCapabilities capabilities = new IOSNFCCapabilities();
	OpeneCardContextConfig config = new OpeneCardContextConfig(IOSNFCFactory.class.getCanonicalName(), JAXBMarshaller.class.getCanonicalName());
	CommonActivationUtils activationUtils = new CommonActivationUtils(config);
	this.capabilities = capabilities;
	this.utils = activationUtils;
    }

    public ContextManager context() {
	return this.utils.context(capabilities);
    }

    public EacControllerFactory eacFactory() {
	return this.utils.eacFactory();
    }

    public PinManagementControllerFactory pinManagementFactory() {
	return this.utils.pinManagementFactory();
    }

}
