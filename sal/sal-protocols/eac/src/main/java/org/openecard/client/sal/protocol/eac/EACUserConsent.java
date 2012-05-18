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
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.*;
import org.openecard.client.gui.executor.ExecutionEngine;
import org.openecard.client.gui.executor.ExecutionResults;
import org.openecard.client.sal.protocol.eac.gui.CHATStep;
import org.openecard.client.sal.protocol.eac.gui.CVCStep;
import org.openecard.client.sal.protocol.eac.gui.GUIContentMap;
import org.openecard.client.sal.protocol.eac.gui.PINStep;


/**
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EACUserConsent {

    private static final Logger logger = LogManager.getLogger(EACUserConsent.class.getName());
    private UserConsent gui;

    protected EACUserConsent(UserConsent gui) {
	this.gui = gui;
    }

    public void show(GUIContentMap content) {

	UserConsentDescription uc = new UserConsentDescription("PACE Protokol");

	CVCStep cvcStep = new CVCStep(content);
	CHATStep chatStep = new CHATStep(content);
	PINStep pinStep = new PINStep(content);

	uc.getSteps().add(cvcStep.create());
	uc.getSteps().add(chatStep.create());
	uc.getSteps().add(pinStep.create());

	UserConsentNavigator navigator = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navigator);
	ResultStatus processResult = exec.process();

	if(processResult.equals(ResultStatus.CANCEL)){
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
