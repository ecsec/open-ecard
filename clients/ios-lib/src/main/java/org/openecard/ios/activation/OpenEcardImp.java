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
package org.openecard.ios.activation;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Provider;
import java.security.Security;
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecard.common.util.SysUtils;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.common.CommonActivationUtils;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.robovm.annotations.FrameworkObject;
import org.openecard.scio.CachingTerminalFactoryBuilder;
import org.openecard.scio.IOSNFCFactory;
import org.openecard.ws.android.AndroidMarshaller;
import org.openecard.scio.IOSConfig;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
@FrameworkObject(factoryMethod = "createOpenEcard")
public class OpenEcardImp implements OpenEcard {

    static {
	// define that this system is iOS
	SysUtils.setIsIOS();

	Provider provider = new BouncyCastleProvider();
	try {
	    Security.removeProvider(provider.getName());
	    Security.removeProvider("BC");
	} catch (Exception e) {
	}
	Security.addProvider(provider);

	LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
	Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
	rootLogger.setLevel(Level.ERROR);

    }

    private final CommonActivationUtils utils;
    private final ContextManager context;
    private String defaultNFCDialogMsg;
    private String defaultNFCCardRecognizedMessage;

    public OpenEcardImp() {
	IOSNFCCapabilities capabilities = new IOSNFCCapabilities();
	IOSConfig currentConfig = new IOSConfig() {
	    @Override
	    public String getDefaultProviderCardMSG() {

		return defaultNFCDialogMsg;
	    }

	    @Override
	    public String getDefaultCardRecognizedMSG() {

		return defaultNFCCardRecognizedMessage;
	    }
	};

	CachingTerminalFactoryBuilder<IOSNFCFactory> builder = new CachingTerminalFactoryBuilder(() -> new IOSNFCFactory(currentConfig));
	OpeneCardContextConfig config = new OpeneCardContextConfig(builder, AndroidMarshaller.class.getCanonicalName());
	CommonActivationUtils activationUtils = new CommonActivationUtils(config, new IOSNFCDialogMsgSetter(builder));
	this.utils = activationUtils;
	this.context = this.utils.context(capabilities);
    }

    @Override
    public ContextManager context(String defaultNFCDialgoMsg, String defaultNFCCardRecognizedMessage) {
	this.defaultNFCDialogMsg = defaultNFCDialgoMsg;
	this.defaultNFCCardRecognizedMessage = defaultNFCCardRecognizedMessage;
	return context;
    }

    @Override
    public String prepareTCTokenURL(String tcTokenURL) {
	try {
	    return URLEncoder.encode(tcTokenURL, "UTF-8");
	} catch (UnsupportedEncodingException ex) {
	    return "ERROR ";
	}
    }

    @Override
    public void setDebugLogLevel() {
	LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
	Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
	rootLogger.setLevel(Level.DEBUG);
    }

}
