/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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

package org.openecard.gui.android.eac;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mockit.Expectations;
import mockit.Mocked;
import org.openecard.gui.StepResult;
import org.openecard.gui.android.GuiIfaceReceiver;
import org.openecard.gui.android.eac.types.PinStatus;
import org.openecard.gui.android.eac.types.ServerData;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.ToggleText;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType;
import org.openecard.sal.protocol.eac.gui.EacPinStatus;
import org.openecard.sal.protocol.eac.gui.PINStep;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich
 */
public class EacGuiImplTest {

    @Mocked
    PACEMarkerType paceMarker;
    @Mocked
    EACData eacData;

    @Test
    public void testPinOkFirstTime() throws InterruptedException {

	new Expectations() {{
	    eacData.passwordType = "PIN";
	    paceMarker.getMinLength(); result = 6;
	    paceMarker.getMaxLength(); result = 6;
	}};

	final GuiIfaceReceiver<EacGuiImpl> guiRec = new GuiIfaceReceiver<>();
	final EacGuiImpl anyGuiImpl = new EacGuiImpl();
	guiRec.setUiInterface(anyGuiImpl);

	Thread t = new Thread(new Runnable() {
	    @Override
	    public void run() {
		UserConsentDescription uc = new UserConsentDescription("Test");
		uc.getSteps().addAll(createInitialSteps());
		EacNavigator nav = new EacNavigator(uc, guiRec);
		ExecutionEngine exe = new ExecutionEngine(nav);
		exe.process();
	    }
	}, "GUI-Executor");
	t.start();

	// use the Binders API to access the values
	ServerData sd = anyGuiImpl.getServerData();
	assertEquals(sd.getSubject(), "Test Subject");
	anyGuiImpl.selectAttributes(sd.getReadAccessAttributes(), sd.getWriteAccessAttributes());
	assertEquals(anyGuiImpl.getPinStatus(), PinStatus.RC3);
	assertTrue(anyGuiImpl.enterPin(null, "123456"));

	// wait for executor to finish
	t.join();
    }

    private List<Step> createInitialSteps() {
	Step step1 = new Step("PROTOCOL_EAC_GUI_STEP_CVC", "CVC");
	ToggleText sub = new ToggleText();
	sub.setID("SubjectName");
	sub.setText("Test Subject");

	step1.getInputInfoUnits().add(sub);

	final Step step2 = new Step("PROTOCOL_EAC_GUI_STEP_CHAT", "CHAT");
	Checkbox readBox = new Checkbox("ReadCHATCheckBoxes");
	readBox.getBoxItems().add(makeBoxItem("DG04", false, false));
	readBox.getBoxItems().add(makeBoxItem("RESTRICTED_IDENTIFICATION", true, true));

	step2.getInputInfoUnits().add(readBox);

	step1.setAction(new StepAction(step1) {
	    @Override
	    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
		return new StepActionResult(StepActionResultStatus.NEXT, step2);
	    }
	});

	final Step step3 = new PINStep(eacData, true, paceMarker, EacPinStatus.RC3);

	step2.setAction(new StepAction(step2) {
	    @Override
	    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
		return new StepActionResult(StepActionResultStatus.NEXT, step3);
	    }
	});

	final Step step4 = new Step("PROTOCOL_GUI_STEP_PROCESSING", "Finished");

	return Arrays.asList(step1, step2, step3, step4);
    }

    private BoxItem makeBoxItem(String value, boolean checked, boolean disabled) {
	BoxItem item = new BoxItem();

	item.setName(value);
	item.setChecked(checked);
	item.setDisabled(disabled);

	return item;
    }

}
