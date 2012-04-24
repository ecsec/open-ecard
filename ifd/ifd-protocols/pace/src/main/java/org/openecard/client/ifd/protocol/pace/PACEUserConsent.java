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
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.Textfield;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.ExecutionEngine;


/**
 * Implements a user consent GUI for the PACE protocol.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PACEUserConsent {

    private final I18n lang = I18n.getTranslation("ifd");

    protected PACEUserConsent() { }

    public byte[] getPINFromUser(UserConsent gui) {
	UserConsentDescription uc = new UserConsentDescription("openecard");
	Step s3 = new Step(lang.translationForKey("pin_entry"));

	Text i9 = new Text();
	i9.setText(lang.translationForKey("pin_agree") + "\n");
	s3.getInputInfoUnits().add(i9);

	Textfield f1 = new Textfield();
	f1.setName(lang.translationForKey("pin"));
	s3.getInputInfoUnits().add(f1);

	uc.getSteps().add(s3);

	UserConsentNavigator navigator = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navigator);
	exec.process();

	Textfield t = (Textfield) exec.getResults().get(lang.translationForKey("pin_entry")).getResults().get(0);
	//TODO error handling
	return t.getValue().getBytes();
    }

}
