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
package org.openecard.mobile.activation.integration;

import java.security.Provider;
import java.security.Security;
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.openecard.mobile.activation.model.DelegatingMobileNfcTerminalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author Neil Crossley
 */
public class BaseIntegrationSetup {

    private static final Logger LOG = LoggerFactory.getLogger(BaseIntegrationSetup.class);

    private MockitoSession mockito;
    private String providerName;

    @BeforeClass
    void initEnvironment() {
	// HACK: too much parallel class loading will cause deadlocks! https://github.com/spring-projects/spring-boot/issues/16744
	boolean requireHack = false;
	if (requireHack) {
	    Provider provider = new BouncyCastleProvider();
	    Security.addProvider(provider);
	    this.providerName = provider.getName();
	}
    }

    @AfterClass()
    void removeEnvironment() {
	if (providerName != null) {
	    Security.removeProvider(providerName);
	}
    }

    @BeforeMethod()
    void setup() {
	mockito = Mockito.mockitoSession()
		.initMocks(this)
		.strictness(Strictness.LENIENT)
		.startMocking();

    }

    @AfterMethod()
    void teardown() {
	try {
	    mockito.finishMocking();

	} catch (Exception e) {
	    LOG.warn("Error occured during cleanup.", e);
	}
	DelegatingMobileNfcTerminalFactory.setDelegate(null);
    }

}
