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

package org.openecard.sal.protocol.eac.gui;

import org.openecard.common.I18n;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.sal.protocol.eac.EACData;


/**
 * PIN GUI step for EAC.
 * This GUI step behaves differently
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PINStep extends Step {

    // step id
    public static final String STEP_ID = "PROTOCOL_EAC_GUI_STEP_PIN";
    // GUI translation constants
    private static final String TITLE = "step_pin_title";
    private static final String DESCRIPTION = "step_pin_description";
    // GUI element IDs
    public static final String PIN_FIELD = "PACE_PIN_FIELD";

    private final I18n lang = I18n.getTranslation("eac");
    private final EACData eacData;

    public PINStep(EACData eacData, boolean capturePin) {
	super(STEP_ID);
	this.eacData = eacData;
	setTitle(lang.translationForKey(TITLE));
	setDescription(lang.translationForKey(DESCRIPTION));

	// create step elements
	if (capturePin) {
	    addSoftwareElements();
	} else {
	    addTerminalElements();
	}
    }

    private void addSoftwareElements() {
	setResetOnLoad(true);
	Text description = new Text();
	description.setText(lang.translationForKey(DESCRIPTION));
	getInputInfoUnits().add(description);

	PasswordField pinInputField = new PasswordField(PIN_FIELD);
	pinInputField.setDescription(lang.translationForKey(eacData.passwordType));
	getInputInfoUnits().add(pinInputField);
    }

    private void addTerminalElements() {
	setInstantReturn(true);
	Text description = new Text();
	// TODO: use translation
	description.setText("Please enter the PIN in your card terminal.");
	getInputInfoUnits().add(description);
    }

}
