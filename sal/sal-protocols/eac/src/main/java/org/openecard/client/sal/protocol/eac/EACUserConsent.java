/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.sal.protocol.eac;

import java.util.Map;
import java.util.logging.Logger;
import org.openecard.client.common.I18n;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.*;
import org.openecard.client.gui.executor.ExecutionEngine;
import org.openecard.client.gui.executor.ExecutionResults;
import org.openecard.client.gui.executor.StepAction;
import org.openecard.client.gui.executor.StepActionResult;
import org.openecard.client.gui.executor.StepActionResultStatus;
import org.openecard.client.sal.protocol.eac.gui.CHATStep;
import org.openecard.client.sal.protocol.eac.gui.CVCStep;
import org.openecard.client.sal.protocol.eac.gui.GUIContentMap;
import org.openecard.client.sal.protocol.eac.gui.PINStep;


/**
 * Implements the EAC user consent dialog.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EACUserConsent {

    private static final Logger logger = LogManager.getLogger(EACUserConsent.class.getName());
    // GUI translation constants
    private static final String TITLE = "eac_user_consent_title";
    private I18n lang = I18n.getTranslation("sal");
    private UserConsent gui;

    /**
     * Creates a new EAC user consent.
     *
     * @param gui GUI
     */
    protected EACUserConsent(UserConsent gui) {
	this.gui = gui;
    }

    /**
     * Shows the GUI.
     *
     * @param content GUI Content
     */
    public void show(GUIContentMap content) {
	final UserConsentDescription uc = new UserConsentDescription(lang.translationForKey(TITLE));

	final CVCStep cvcStep = new CVCStep(content);
	final CHATStep chatStep = new CHATStep(content);
	final PINStep pinStep = new PINStep(content);

	uc.getSteps().add(cvcStep.getStep());
	uc.getSteps().add(chatStep.getStep());
	uc.getSteps().add(pinStep.getStep());

	// Custom action for PIN step
	StepAction chatStepAction = new StepAction(chatStep.getStep()) {

	    @Override
	    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
		Checkbox cc = null;
		for (OutputInfoUnit o : result.getResults()) {
		    if (o instanceof Checkbox) {
			cc = (Checkbox) o;
		    }
		}

		switch (result.getStatus()) {
		    case BACK:
			return new StepActionResult(StepActionResultStatus.BACK);
		    case OK:
			for (InputInfoUnit i : pinStep.getStep().getInputInfoUnits()) {
			    if (i instanceof Checkbox) {
				Checkbox c = (Checkbox) i;
				c.getBoxItems().clear();
				for (BoxItem b : cc.getBoxItems()) {
				    if (b.isChecked()) {
					BoxItem ii = b;
					ii.setDisabled(true);
					c.getBoxItems().add(ii);
				    }
				}
			    }
			}
			return new StepActionResult(StepActionResultStatus.NEXT);
		    default:
			return new StepActionResult(StepActionResultStatus.REPEAT);
		}
	    }
	};

	// Custom action for PIN step
	StepAction pinStepAction = new StepAction(pinStep.getStep()) {

	    @Override
	    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
		Checkbox cc = null;
		for (OutputInfoUnit o : oldResults.get(chatStep.getStep().getID()).getResults()) {
		    if (o instanceof Checkbox) {
			cc = (Checkbox) o;
		    }
		}

		switch (result.getStatus()) {
		    case BACK:
			for (InputInfoUnit i : chatStep.getStep().getInputInfoUnits()) {
			    if (i instanceof Checkbox) {
				Checkbox c = (Checkbox) i;
				c.getBoxItems().clear();
				for (BoxItem b : cc.getBoxItems()) {
				    BoxItem ii = b;
				    ii.setDisabled(false);
				    ii.setChecked(b.isChecked());
				    c.getBoxItems().add(ii);
				}
			    }
			}
			return new StepActionResult(StepActionResultStatus.BACK);
		    case OK:
			return new StepActionResult(StepActionResultStatus.NEXT);
		    default:
			return new StepActionResult(StepActionResultStatus.REPEAT);
		}
	    }
	};

	UserConsentNavigator navigator = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navigator);

	// Add custom action
	exec.addCustomAction(chatStepAction);
	exec.addCustomAction(pinStepAction);

	ResultStatus processResult = exec.process();

	if (processResult.equals(ResultStatus.CANCEL)) {
	    //TODO
//	    throw new WSHelper.WSException(WSHelper.makeResultError(
//			ECardConstants.Minor.IFD.CANCELLATION_BY_USER,""));
	}

	Map<String, ExecutionResults> results = exec.getResults();
	cvcStep.processResult(results);
	chatStep.processResult(results);
	pinStep.processResult(results);
    }
}
