/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.ifd.protocol.pace;

import java.util.logging.Logger;
import org.openecard.client.common.I18n;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.ExecutionEngine;
import org.openecard.client.ifd.protocol.pace.gui.GUIContentMap;
import org.openecard.client.ifd.protocol.pace.gui.PINStep;


/**
 * Implements a user consent GUI for the PACE protocol.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACEUserConsent {

    private static final Logger logger = LogManager.getLogger(PACEUserConsent.class.getName());
    private final I18n lang = I18n.getTranslation("ifd");
    private UserConsent gui;

    protected PACEUserConsent(UserConsent gui) {
	this.gui = gui;
    }

    public void show(GUIContentMap content) {
	PINStep pinStep = new PINStep(content);

	UserConsentDescription uc = new UserConsentDescription("PACE Protocol");
	uc.getSteps().add(pinStep.create());

	UserConsentNavigator navigator = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navigator);
	exec.process();

	pinStep.processResult(exec.getResults());
    }
}
