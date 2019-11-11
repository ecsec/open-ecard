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

import org.openecard.mobile.activation.ActivationResultCode;
import org.openecard.mobile.activation.model.World;
import org.openecard.mobile.activation.model.WorldBuilder;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class EacContextTests extends BaseIntegrationSetup {

    @Test
    void canBeginEacActivation() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.eacWorld.startSimpleEac();

	    world.eacWorld.expectOnStarted();
	}
    }

    @Test
    void whenEacActivationCancelsThenEacActivationIsInterrupted() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.eacWorld.startSimpleEac();

	    world.eacWorld.cancelEac();

	    world.eacWorld.expectActivationResult(ActivationResultCode.INTERRUPTED);
	}
    }

    @Test
    void expectServerData() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.eacWorld.startSimpleEac();

	    world.eacWorld.expectOnStarted();

	    world.microSleep();
	    world.microSleep();

	    world.eacWorld.expectOnServerData();
	}
    }

    @Test
    void expectCardRequest() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.eacWorld.startSimpleEac();

	    world.eacWorld.expectOnStarted();

	    world.microSleep();
	    world.microSleep();
	    world.microSleep();

	    world.eacWorld.expectOnServerData();

	    world.microSleep();
	    world.microSleep();

	    world.eacWorld.givenConfirmationOfServerData();

	    world.microSleep();
	    world.microSleep();

	    world.eacWorld.expectCardInsertionRequest();
	}
    }

    @Test
    void canSuccessfullyChangePin() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.eacWorld.startSimpleEac();

	    world.eacWorld.expectOnStarted();

	    world.eacWorld.expectCardInsertionRequest();

	    world.givenNpaCardInserted();

	    world.eacWorld.expectSuccessfulPinEntry();
	}
    }

    @Test
    @Ignore("Eac activation  has not been reworked.")
    void incorrectPinChangeWillFail() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.eacWorld.startSimpleEac();

	    world.eacWorld.expectOnStarted();

	    world.eacWorld.expectCardInsertionRequest();

	    world.givenNpaCardInserted();

	    world.eacWorld.expectIncorrectPinEntryToFail();
	}
    }

    @Test
    @Ignore("Eac activation has not been reworked.")
    void expectNpaCardRecognition() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.eacWorld.startSimpleEac();

	    world.eacWorld.expectOnStarted();

	    world.givenNpaCardInserted();

	    world.eacWorld.expectRecognitionOfNpaCard();
	}
    }

    @Test
    @Ignore("Eac activation has not been reworked.")
    void investigateMultipleNpaCardRecognitions() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.eacWorld.startSimpleEac();

	    world.eacWorld.expectOnStarted();

	    world.givenNpaCardInserted();
	    world.eacWorld.expectRecognitionOfNpaCard();

	    world.microSleep();

	    world.givenaCardRemoved();
	    world.eacWorld.expectRemovalOfCard();

	    world.givenNpaCardInserted();
	    world.eacWorld.expectRecognitionOfNpaCard();

	    world.microSleep();

	    world.givenaCardRemoved();
	    world.eacWorld.expectRemovalOfCard();
	}
    }
}
