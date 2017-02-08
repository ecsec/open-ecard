/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.addons.cg.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class ChipGatewayProperties {

    private static final Logger LOG = LoggerFactory.getLogger(ChipGatewayProperties.class);
    private static ChipGatewayProperties INST;

    private final Properties props;

    private ChipGatewayProperties() {
	this.props = new Properties();
    }

    private ChipGatewayProperties(String propsResource) throws IOException {
	InputStream is = getClass().getResourceAsStream(propsResource);
	this.props = new Properties();
	this.props.load(is);
    }

    private static synchronized ChipGatewayProperties getInstance() {
	if (INST == null) {
	    try {
		INST = new ChipGatewayProperties("/chipgateway/cg_policy.properties");
	    } catch (IOException ex) {
		LOG.error("The bundled properties file could not be loaded.", ex);
		INST = new ChipGatewayProperties();
	    }
	}
	return INST;
    }

    public static boolean isRemotePinAllowed() {
	String pinAllowedStr = getInstance().props.getProperty("remote-pin-allowed", "false");
	return Boolean.parseBoolean(pinAllowedStr);
    }

    public static boolean isValidateServerCert() {
	String validateStr = getInstance().props.getProperty("validate-server-cert", "true");
	return Boolean.parseBoolean(validateStr);
    }

    public static boolean isValidateChallengeResponse() {
	String validateStr = getInstance().props.getProperty("validate-challenge-response", "true");
	return Boolean.parseBoolean(validateStr);
    }

    public static boolean isRevocationCheck() {
	String revocationStr = getInstance().props.getProperty("revocation-check", "true");
	return Boolean.parseBoolean(revocationStr);
    }

    public static boolean isUseSubjectWhitelist() {
	String whitelistStr = getInstance().props.getProperty("use-subject-whitelist", "true");
	return Boolean.parseBoolean(whitelistStr);
    }

    public static boolean isUseApiEndpointWhitelist() {
	String whitelistStr = getInstance().props.getProperty("use-api-endpoint-whitelist", "true");
	return Boolean.parseBoolean(whitelistStr);
    }

    public static boolean isDeveloperTrustStore() {
	String devTrustStoreStr = getInstance().props.getProperty("developer-truststore", "false");
	return Boolean.parseBoolean(devTrustStoreStr);
    }

    public static boolean isUseUpdateDomainWhitelist() {
	String devTrustStoreStr = getInstance().props.getProperty("use-update-domain-whitelist", "true");
	return Boolean.parseBoolean(devTrustStoreStr);
    }

}
