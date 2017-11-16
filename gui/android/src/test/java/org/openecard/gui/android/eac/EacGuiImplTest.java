/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

import android.content.Context;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.openecard.gui.StepResult;
import org.openecard.gui.android.eac.types.ServerData;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.ToggleText;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich
 */
public class EacGuiImplTest {

    @Mocked
    Context androidCtx;
    @Mocked
    UserConsentDescription ucd;
    @Mocked
    EacGui.Stub stub;

    @Test
    public void testGivenCorrectValuesThenCreateFromShouldBeCorrect() {
	final List<Step> expectedSteps = createInitialSteps();
	new Expectations() {{
	    ucd.getDialogType(); result = "EAC";
	    ucd.getSteps(); result = expectedSteps;
	}};
	EacGuiService.prepare();
	
	final EacNavigator result = EacNavigator.createFrom(androidCtx, ucd);
	
	assertNotNull(result);
	assertTrue(result.hasNext());
    }
    
    @Test
    public void testGivenCorrectValuesThenCreateFromShouldStoreNewGui() {
	final List<Step> expectedSteps = createInitialSteps();
	EacGuiService.prepare();

	new Expectations() {{
	    ucd.getDialogType(); result = "EAC";
	    ucd.getSteps(); result = expectedSteps;
	    
	    EacGuiService singleton;
	    {
		EacGuiService.setGuiImpl((EacGuiImpl)any);
	    }
	}};
	
	EacGuiService.prepare();
	EacNavigator.createFrom(androidCtx, ucd);
    }
    
    @Test
    public void testPinOkFirstTime() throws InterruptedException, RemoteException {
	EacGuiService.prepare();
	
	final EacGuiImpl anyGuiImpl = new EacGuiImpl();
	
	Thread t = new Thread(new Runnable() {
	    @Override
	    public void run() {
		EacNavigator nav = new EacNavigator(anyGuiImpl, new ArrayList<>(createInitialSteps()));
		ExecutionEngine exe = new ExecutionEngine(nav);
		exe.process();
	    }
	}, "GUI-Executor");
	t.start();

	// use the Binders API to access the values
	ServerData sd = anyGuiImpl.getServerData();
	assertEquals(sd.getSubject(), "Test Subject");
	anyGuiImpl.selectAttributes(sd.getReadAccessAttributes(), sd.getWriteAccessAttributes());
	assertEquals(anyGuiImpl.getPinStatus(), "PIN");
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

	final Step step3 = new Step("PROTOCOL_EAC_GUI_STEP_PIN", "PIN");
	PasswordField pin = new PasswordField("PACE_PIN_FIELD");
	step3.getInputInfoUnits().add(pin);

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
