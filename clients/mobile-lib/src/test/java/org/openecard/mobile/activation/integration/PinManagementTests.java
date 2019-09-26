/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.mobile.activation.integration;

import org.openecard.mobile.activation.ActivationResultCode;
import org.openecard.mobile.activation.model.World;
import org.openecard.mobile.activation.model.WorldBuilder;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class PinManagementTests extends BaseIntegrationTests {

    @Test
    void canBeginPinManagement() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.pinManagementWorld.startSimplePinManagement();

	    world.pinManagementWorld.expectOnStarted();
	}
    }

    @Test
    void whenPinManagementCancelsThenPinManagementIsInterrupted() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.pinManagementWorld.startSimplePinManagement();

	    world.pinManagementWorld.cancelPinManagement();

	    world.pinManagementWorld.expectActivationResult(ActivationResultCode.INTERRUPTED);
	}
    }

    @Test
    @Ignore("Card insertion request is currently not called.")
    void expectCardRequest() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.pinManagementWorld.startSimplePinManagement();

	    world.pinManagementWorld.expectOnStarted();

	    world.pinManagementWorld.expectCardInsertionRequest();
	}
    }

    @Test
    @Ignore("Pin management has not been reworked.")
    void canSuccessfullyChangePin() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.pinManagementWorld.startSimplePinManagement();

	    world.pinManagementWorld.expectOnStarted();

	    world.pinManagementWorld.expectCardInsertionRequest();

	    world.givenNpaCardInserted();

	    world.pinManagementWorld.expectSuccessfulPinChange();
	}
    }

    @Test
    @Ignore("Pin management has not been reworked.")
    void incorrectPinChangeWillFail() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.pinManagementWorld.startSimplePinManagement();

	    world.pinManagementWorld.expectOnStarted();

	    world.pinManagementWorld.expectCardInsertionRequest();

	    world.givenNpaCardInserted();

	    world.pinManagementWorld.expectIncorrectPinChangeToFail();
	}
    }

    @Test
    void expectNpaCardRecognition() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.pinManagementWorld.startSimplePinManagement();

	    world.pinManagementWorld.expectOnStarted();

	    world.givenNpaCardInserted();

	    world.pinManagementWorld.expectRecognitionOfNpaCard();
	}
    }
}
