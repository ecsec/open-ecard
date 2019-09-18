/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.common;

import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.openecard.mobile.activation.ActivationResultCode;
import org.openecard.mobile.activation.model.DelegatingMobileNfcTerminalFactory;
import org.openecard.mobile.activation.model.World;
import org.openecard.mobile.activation.model.WorldBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class IntegrationTests {

    private static final Logger LOG = LoggerFactory.getLogger(CommonContextManagerWithFakeNfcTests.class);

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

    @Test
    void canStartWorld() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create().withMobileNfcStack();
	try ( World world = worldBuilder.build()) {
	    world.contextWorld.startSuccessfully();
	}
    }

    @Test
    void canStartAndStopWorld() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create().withMobileNfcStack();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();
	    world.contextWorld.stopSuccessfully();
	}
    }

    @Test
    void canStartThenSleepAndStopWorld() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create().withMobileNfcStack();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();
	    world.microSleep();
	    world.contextWorld.stopSuccessfully();
	}
    }

    @Test
    void canBeginPinManagement() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create().withMobileNfcStack();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.pinManagementWorld.startSimplePinManagement();
	}
    }

    @Test
    void WhenPinManagementCancelsThenPinManagementIsInterrupted() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create().withMobileNfcStack();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.microSleep();

	    world.pinManagementWorld.startSimplePinManagement();

	    world.microSleep();

	    world.pinManagementWorld.cancelPinManagement();

	    world.pinManagementWorld.expectActivationResult(ActivationResultCode.INTERRUPTED);
	}
    }

}
