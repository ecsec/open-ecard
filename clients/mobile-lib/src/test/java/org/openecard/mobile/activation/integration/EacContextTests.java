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

import org.openecard.mobile.activation.model.World;
import org.openecard.mobile.activation.model.WorldBuilder;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class EacContextTests extends BaseIntegrationTests {

    @Test
    void canStartWorld() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {
	    world.contextWorld.startSuccessfully();
	}
    }

    @Test
    void canStartAndStopWorld() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();
	    world.contextWorld.stopSuccessfully();
	}
    }

    @Test
    void canStartThenSleepAndStopWorld() throws Exception {
	WorldBuilder worldBuilder = WorldBuilder.create();
	try ( World world = worldBuilder.build()) {

	    world.contextWorld.startSuccessfully();

	    world.microSleep();

	    world.contextWorld.stopSuccessfully();
	}
    }

}
