/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
import java.util.Map;
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
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class CHATStep extends Step {

    private static final I18n lang = I18n.getTranslation("eac");
    // step id
    public static final String STEP_ID = "PROTOCOL_EAC_GUI_STEP_CHAT";
    // GUI translation constants
    public static final String TITLE = "step_chat_title";
    public static final String STEP_DESCRIPTION = "step_chat_step_description";
    public static final String DESCRIPTION = "step_chat_description";
    public static final String NOTE = "step_chat_note";
    public static final String NOTE_CONTENT = "step_chat_note_content";
    public static final String READ_ACCESS_DESC = "step_chat_read_access_description";
    public static final String WRITE_ACCESS_DESC = "step_chat_write_access_description";
    // GUI element IDs
    public static final String READ_CHAT_BOXES = "ReadCHATCheckBoxes";
    public static final String WRITE_CHAT_BOXES = "WriteCHATCheckBoxes";

    private final EACData eacData;

    public CHATStep(EACData eacData) {
	super(STEP_ID);
	this.eacData = eacData;
	setTitle(lang.translationForKey(TITLE));
	setDescription(lang.translationForKey(STEP_DESCRIPTION));

	// create step elements
	addElements();
    }

    public static Step createDummy() {
	Step s = new Step(STEP_ID);
	s.setTitle(lang.translationForKey(TITLE));
	s.setDescription(lang.translationForKey(STEP_DESCRIPTION));
	return s;
    }

    private void addElements() {
	Text decription = new Text();
	String decriptionText = lang.translationForKey(DESCRIPTION, eacData.certificateDescription.getSubjectName());
	decription.setText(decriptionText);
	getInputInfoUnits().add(decription);

	// process read access and special functions
	Checkbox readAccessCheckBox = new Checkbox(READ_CHAT_BOXES);
	boolean displayReadAccessCheckBox = false;
	readAccessCheckBox.setGroupText(lang.translationForKey(READ_ACCESS_DESC));
	Map<CHAT.DataGroup, Boolean> requiredReadAccess = eacData.requiredCHAT.getReadAccess();
	Map<CHAT.DataGroup, Boolean> optionalReadAccess = eacData.optionalCHAT.getReadAccess();
	Map<CHAT.SpecialFunction, Boolean> requiredSpecialFunctions = eacData.requiredCHAT.getSpecialFunctions();
	Map<CHAT.SpecialFunction, Boolean> optionalSpecialFunctions = eacData.optionalCHAT.getSpecialFunctions();

	// iterate over all 21 eID application data groups
	for (CHAT.DataGroup dataGroup : requiredReadAccess.keySet()) {
	    if (TR03119RightsFilter.isTr03119ConformReadRight(dataGroup)) {
		if (requiredReadAccess.get(dataGroup)) {
		    displayReadAccessCheckBox = true;
		    readAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, true));
		} else if (optionalReadAccess.get(dataGroup)) {
		    displayReadAccessCheckBox = true;
		    readAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, false));
		}
	    } else {
		eacData.requiredCHAT.setReadAccess(dataGroup, false);
	    }
	}

	// iterate over all 8 special functions
	for (CHAT.SpecialFunction specialFunction : requiredSpecialFunctions.keySet()) {
	    // determine if extra data is necessary
	    Object[] textData = new Object[0];
	    if (CHAT.SpecialFunction.AGE_VERIFICATION == specialFunction) {
		Calendar c = eacData.aad.getAgeVerificationData();
		if (c != null) {
		    int yearDiff = getYearDifference(c);
		    textData = new Object[] { yearDiff };
		}
	    }

	    if (TR03119RightsFilter.isTr03119ConformSpecialFunction(specialFunction)) {
		if (requiredSpecialFunctions.get(specialFunction)) {
		    displayReadAccessCheckBox = true;
		    readAccessCheckBox.getBoxItems().add(makeBoxItem(specialFunction, true, true, textData));
		} else if (optionalSpecialFunctions.get(specialFunction)) {
		    displayReadAccessCheckBox = true;
		    readAccessCheckBox.getBoxItems().add(makeBoxItem(specialFunction, true, false, textData));
		}
	    } else {
		eacData.requiredCHAT.setSpecialFunctions(specialFunction, false);
	    }
	}

	if (displayReadAccessCheckBox) {
	    getInputInfoUnits().add(readAccessCheckBox);
	}

	// process write access
	Checkbox writeAccessCheckBox = new Checkbox(WRITE_CHAT_BOXES);
	boolean displayWriteAccessCheckBox = false;
	writeAccessCheckBox.setGroupText(lang.translationForKey(WRITE_ACCESS_DESC));
	Map<CHAT.DataGroup, Boolean> requiredWriteAccess = eacData.requiredCHAT.getWriteAccess();
	Map<CHAT.DataGroup, Boolean> optionalWriteAccess = eacData.optionalCHAT.getWriteAccess();

	// iterate over DG17-DG21 of the eID application data groups
	for (CHAT.DataGroup dataGroup : requiredWriteAccess.keySet()) {
	    if (TR03119RightsFilter.isTr03119ConformWriteRight(dataGroup)) {
		if (requiredWriteAccess.get(dataGroup)) {
		    displayWriteAccessCheckBox = true;
		    writeAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, true));
		} else if (optionalWriteAccess.get(dataGroup)) {
		    displayWriteAccessCheckBox = true;
		    writeAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, false));
		}
	    } else {
		eacData.requiredCHAT.setWriteAccess(dataGroup, false);
	    }
	}

	if (displayWriteAccessCheckBox) {
	    getInputInfoUnits().add(writeAccessCheckBox);
	}

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
