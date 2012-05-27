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

import org.openecard.client.common.I18n;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.ExecutionEngine;
import org.openecard.client.ifd.protocol.pace.gui.GUIContentMap;
import org.openecard.client.ifd.protocol.pace.gui.PINStep;


/**
 * Implements a user consent GUI for the PACE protocol.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACEUserConsent {

    // GUI translation constants
    private static final String USER_CONSENT = "step_pace_userconsent";
    //
    private final I18n lang = I18n.getTranslation("ifd");
    private UserConsent gui;

    /**
     * Creates a new PACEUserConsent.
     *
     * @param gui GUI
     */
    protected PACEUserConsent(UserConsent gui) {
	this.gui = gui;
    }

    /**
     * Shows the user consent.
     *
     * @param content GUI content
     */
    public void show(GUIContentMap content) {
	final UserConsentDescription uc = new UserConsentDescription(lang.translationForKey(USER_CONSENT));

	final PINStep pinStep = new PINStep(content);

	uc.getSteps().add(pinStep.getStep());

	UserConsentNavigator navigator = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navigator);
	exec.process();

	pinStep.processResult(exec.getResults());
    }
}
