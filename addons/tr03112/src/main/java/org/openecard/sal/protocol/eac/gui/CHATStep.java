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

import java.util.Calendar;
import java.util.TreeMap;
import org.openecard.common.I18n;
import org.openecard.crypto.common.asn1.cvc.CHAT;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.ToggleText;
import org.openecard.sal.protocol.eac.EACData;


/**
 * CHAT GUI step for EAC.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CHATStep extends Step {

    // step id
    public static final String STEP_ID = "PROTOCOL_EAC_GUI_STEP_CHAT";
    // GUI translation constants
    public static final String TITLE = "step_chat_title";
    public static final String DESCRIPTION = "step_chat_description";
    public static final String NOTE = "step_chat_note";
    public static final String NOTE_CONTENT = "step_chat_note_content";
    // GUI element IDs
    public static final String CHAT_BOXES = "CHATCheckBoxs";

    private final I18n lang = I18n.getTranslation("eac");
    private final EACData eacData;

    public CHATStep(EACData eacData) {
	super(STEP_ID);
	this.eacData = eacData;
	setTitle(lang.translationForKey(TITLE));
	setDescription(lang.translationForKey(DESCRIPTION));

	// create step elements
	addElements();
    }

    private void addElements() {
	Text decription = new Text();
	String decriptionText = lang.translationForKey(DESCRIPTION, eacData.certificateDescription.getSubjectName());
	decription.setText(decriptionText);
	getInputInfoUnits().add(decription);

	Checkbox readAccessCheckBox = new Checkbox(CHAT_BOXES);
	TreeMap<CHAT.DataGroup, Boolean> requiredReadAccess = eacData.requiredCHAT.getReadAccess();
	TreeMap<CHAT.DataGroup, Boolean> optionalReadAccess = eacData.optionalCHAT.getReadAccess();
	TreeMap<CHAT.SpecialFunction, Boolean> requiredSpecialFunctions = eacData.requiredCHAT.getSpecialFunctions();
	TreeMap<CHAT.SpecialFunction, Boolean> optionalSpecialFunctions = eacData.optionalCHAT.getSpecialFunctions();

	CHAT.DataGroup[] dataGroups = CHAT.DataGroup.values();
	CHAT.SpecialFunction[] specialFunctions = CHAT.SpecialFunction.values();

	// iterate over all 21 eID application data groups
	for (int i = 0; i < 21; i++) {
	    CHAT.DataGroup dataGroup = dataGroups[i];
	    if (requiredReadAccess.get(dataGroup)) {
		readAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, true));
	    } else if (optionalReadAccess.get(dataGroup)) {
		readAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, false));
	    }
	}

	// iterate over all 8 special functions
	for (int i = 0; i < 8; i++) {
	    CHAT.SpecialFunction specialFunction = specialFunctions[i];

	    // determine if extra data is necessary
	    Object[] textData = new Object[0];
	    if (CHAT.SpecialFunction.AGE_VERIFICATION == specialFunction) {
		Calendar c = eacData.aad.getAgeVerificationData();
		if (c != null) {
		    int yearDiff = getYearDifference(c);
		    textData = new Object[] { yearDiff };
		}
	    }

	    if (requiredSpecialFunctions.get(specialFunction)) {
		readAccessCheckBox.getBoxItems().add(makeBoxItem(specialFunction, true, true, textData));
	    } else if (optionalSpecialFunctions.get(specialFunction)) {
		readAccessCheckBox.getBoxItems().add(makeBoxItem(specialFunction, true, false, textData));
	    }
	}

	getInputInfoUnits().add(readAccessCheckBox);

	ToggleText requestedDataDescription = new ToggleText();
	requestedDataDescription.setTitle(lang.translationForKey(NOTE));
	requestedDataDescription.setText(lang.translationForKey(NOTE_CONTENT));
	requestedDataDescription.setCollapsed(!true);
	getInputInfoUnits().add(requestedDataDescription);
    }

    private BoxItem makeBoxItem(Enum<?> value, boolean checked, boolean disabled, Object... textData) {
	BoxItem item = new BoxItem();

	item.setName(value.name());
	item.setChecked(checked);
	item.setDisabled(disabled);
	item.setText(lang.translationForKey(value.name(), textData));

	return item;
    }

    private static int getYearDifference(Calendar c) {
	Calendar now = Calendar.getInstance();
	now.add(Calendar.DAY_OF_MONTH, -1 * c.get(Calendar.DAY_OF_MONTH));
	now.add(Calendar.DAY_OF_MONTH, 1);
	now.add(Calendar.MONTH, -1 * c.get(Calendar.MONTH));
	now.add(Calendar.MONTH, 1);
	now.add(Calendar.YEAR, -1 * c.get(Calendar.YEAR));
	return now.get(Calendar.YEAR);
    }

}
