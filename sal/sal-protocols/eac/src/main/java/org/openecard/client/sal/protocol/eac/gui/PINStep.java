/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.sal.protocol.eac.gui;

import java.util.Map;
import org.openecard.client.common.I18n;
import org.openecard.client.gui.definition.PasswordField;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.executor.ExecutionResults;
import org.openecard.client.sal.protocol.eac.common.PasswordID;


/**
 * Implements a GUI user consent step for the PIN.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PINStep {

    // GUI translation constants
    private static final String TITLE = "step_pin_title";
    private static final String DESCRIPTION = "step_pin_description";

    private I18n lang = I18n.getTranslation("sal");
    private Step step = new Step(lang.translationForKey(TITLE));
    private GUIContentMap content;
    private String passwordType;

    /**
     * Creates a new GUI user consent step for the PIN.
     *
     * @param content GUI content
     */
    public PINStep(GUIContentMap content) {
	this.content = content;

	passwordType = PasswordID.parse((Byte) (content.get(GUIContentMap.ELEMENT.PIN_ID))).getString();
	initialize();
    }

    private void initialize() {
	step.setResetOnLoad(true);
	Text description = new Text();
	description.setText(lang.translationForKey(DESCRIPTION));
	step.getInputInfoUnits().add(description);

	PasswordField pinInputField = new PasswordField(passwordType);
	pinInputField.setDescription(lang.translationForKey(passwordType));
	step.getInputInfoUnits().add(pinInputField);
    }

    /**
     * Returns the generated step.
     *
     * @return Step
     */
    public Step getStep() {
	return step;
    }

    /**
     * Processes the results of step.
     *
     * @param results Results
     */
    public void processResult(Map<String, ExecutionResults> results) {
	ExecutionResults executionResults = results.get(step.getID());

	if (executionResults == null) {
	    return;
	}

	PasswordField p = (PasswordField) executionResults.getResult(passwordType);
	content.add(GUIContentMap.ELEMENT.PIN, p.getValue());
    }

}
