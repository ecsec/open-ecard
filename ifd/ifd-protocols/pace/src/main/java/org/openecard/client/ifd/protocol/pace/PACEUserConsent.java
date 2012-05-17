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
import org.openecard.client.gui.definition.PasswordField;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.ExecutionEngine;
import org.openecard.client.gui.executor.ExecutionResults;


/**
 * Implements a user consent GUI for the PACE protocol.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACEUserConsent {

    private static final String PIN_ENTRY = "pin_entry";
    private static final String PIN_AGREE = "pin_agree";
    private static final String PIN = "pin";
    private final I18n lang = I18n.getTranslation("ifd");

    protected PACEUserConsent() {
    }

    public String getPINFromUser(UserConsent gui) {
	UserConsentDescription uc = new UserConsentDescription("PACE Protokol");

	Step step = new Step(lang.translationForKey(PIN_ENTRY));

	Text description = new Text();
	description.setText(lang.translationForKey(PIN_AGREE));
	step.getInputInfoUnits().add(description);

	PasswordField pinInputField = new PasswordField();
	pinInputField.setID(PIN_ENTRY);
	pinInputField.setDescription(lang.translationForKey(PIN));
	step.getInputInfoUnits().add(pinInputField);

	uc.getSteps().add(step);

	UserConsentNavigator navigator = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navigator);
	exec.process();

	ExecutionResults execResults = exec.getResults().get(step.getID());
	pinInputField = (PasswordField) execResults.getResults().get(0);

	return pinInputField.getValue();
    }
}
