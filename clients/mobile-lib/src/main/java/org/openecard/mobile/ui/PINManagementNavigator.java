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

package org.openecard.mobile.ui;

import iso.std.iso_iec._24727.tech.schema.PowerDownDevices;
import iso.std.iso_iec._24727.tech.schema.PrepareDevices;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import jdk.internal.org.jline.utils.Log;
import org.openecard.common.DynamicContext;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.Promise;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.mobile.activation.ConfirmOldSetNewPasswordOperation;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.PinManagementInteraction;
import org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction;
import org.openecard.plugins.pinplugin.RecognizedState;
import org.openecard.plugins.pinplugin.gui.GenericPINStep;
import org.openecard.sal.protocol.eac.gui.PinState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Sebastian Schuberth
 * @author Tobias Wich
 */
public class PINManagementNavigator extends MobileNavigator {

    private static final Logger LOG = LoggerFactory.getLogger(PINManagementNavigator.class);

    private final List<Step> steps;
    private final PinManagementInteraction interaction;

    private int idx = -1;
    private Thread pMgmtNextThread;
    private Dispatcher dispatcher;


    public PINManagementNavigator(UserConsentDescription uc, PinManagementInteraction interaction, Dispatcher dispatcher) {
	this.steps = new ArrayList<>(uc.getSteps());
	this.interaction = interaction;
	this.dispatcher = dispatcher;
    }

    @Override
    public boolean hasNext() {
	return idx < (steps.size() - 1);
    }

    @Override
    public StepResult current() {
	// reduce index by one and call next which increases idx by one
	// --> (-1 + 1 = 0)
	idx--;
	return next();
    }

    @Override
    public StepResult next() {
	LOG.debug("started");
	// handle step display
	Step curStep = steps.get(0);

	for (Step s : this.steps) {
	    LOG.debug("Step: {}", s.getDescription());
	}

	// TODO: remove this statement and implement it properly
//	return new MobileResult(pinStep, ResultStatus.INTERRUPTED, Collections.EMPTY_LIST);
	FutureTask<StepResult> pMgmtNext = new FutureTask<>(() -> nextInt(curStep));
	try {
	    // run next in thread and wait for completion
	    // note that promise does not allow to access the result in case of a cancellation
	    pMgmtNextThread = new Thread(pMgmtNext, "PIN-Mgmt-Next");
	    pMgmtNextThread.start();
	    LOG.debug("Waiting for next GUI step to finish.");
	    pMgmtNextThread.join();
	    LOG.debug("Next GUI step finished.");
	    return pMgmtNext.get();
	} catch (InterruptedException ex) {
	    LOG.debug("Waiting for next GUI step interrupted, interrupting the GUI step processing.");
	    pMgmtNextThread.interrupt();
	    try {
		// wait again after interrupting the thread
		LOG.debug("Waiting again for next GUI step to finish.");
		pMgmtNextThread.join();
		LOG.debug("Next GUI step finished.");
		return pMgmtNext.get();
	    } catch (InterruptedException exIn) {
		return new MobileResult(curStep, ResultStatus.INTERRUPTED, Collections.emptyList());
	    } catch (ExecutionException exIn) {
		LOG.error("Unexpected exception occurred in UI Step.", ex);
		return new MobileResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
	    }
	} catch (ExecutionException ex) {
	    LOG.error("Unexpected exception occurred in UI Step.", ex);
	    return new MobileResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
	} finally {
	    pMgmtNextThread = null;
	}

//	return displayAndExecuteBackground(pinStep, () -> {
//	    DynamicContext ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.DYNCTX_INSTANCE_KEY);
//	    RecognizedState uiPinState = (RecognizedState) ctx.get(GetCardsAndPINStatusAction.PIN_STATUS);
//	    Boolean pinCorrect = (Boolean) ctx.get(GetCardsAndPINStatusAction.PIN_CORRECT);
//	    Boolean canCorrect = (Boolean) ctx.get(GetCardsAndPINStatusAction.CAN_CORRECT);
//	    Boolean pukCorrect = (Boolean) ctx.get(GetCardsAndPINStatusAction.PUK_CORRECT);
//
//	    if (uiPinState == null || uiPinState == RecognizedState.UNKNOWN) {
//		LOG.error("No pin state received from UI.");
//		return new MobileResult(pinStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
//	    }
//
//	    // set pin state
//	    this.guiService.sendPinStatus(uiPinState);
//
//	    // set result values if any
//	    if (pinCorrect != null) {
//		this.guiService.setPinCorrect(pinCorrect);
//	    } else if (canCorrect != null) {
//		this.guiService.setCanCorrect(canCorrect);
//	    } else if (pukCorrect != null) {
//		this.guiService.setPukCorrect(pukCorrect);
//	    }
//
//	    // pin accepted or card blocked
//	    if ("success".equals(pinStep.getID())) {
//		return new MobileResult(pinStep, ResultStatus.OK, Collections.EMPTY_LIST);
//	    } else if ("error".equals(pinStep.getID())) {
//		//this.guiService.waitForUserCancel();
//		return new MobileResult(pinStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
//	    }
//
//	    // ask user for the pin
//	    try {
//		List<OutputInfoUnit> outInfo = this.guiService.getPinResult(pinStep);
//		writeBackValues(pinStep.getInputInfoUnits(), outInfo);
//		return new MobileResult(pinStep, ResultStatus.OK, outInfo);
//	    } catch (InterruptedException ex) {
//		return new MobileResult(pinStep, ResultStatus.INTERRUPTED, Collections.EMPTY_LIST);
//	    }
//	});
    }

    private StepResult askForPIN(Step curStep, int attempt) throws InterruptedException {
	interaction.requestCardInsertion();
	Promise<List<OutputInfoUnit>> waitForPIN = new Promise<>();
	interaction.onPinChangeable(attempt, new ConfirmOldSetNewPasswordOperationPINMgmtImpl(waitForPIN));

	List<OutputInfoUnit> result = waitForPIN.deref();
	this.dispatcher.safeDeliver(new PrepareDevices());
	return new MobileResult(curStep, ResultStatus.OK, result);
    }

    private StepResult askForCAN(Step curStep) throws InterruptedException {
	interaction.requestCardInsertion();
	Promise<List<OutputInfoUnit>> waitForCAN = new Promise<>();
	interaction.onCanRequired(new ConfirmPasswordOperationPINMgmtImpl(waitForCAN, GenericPINStep.CAN_FIELD));

	List<OutputInfoUnit> result = waitForCAN.deref();
	this.dispatcher.safeDeliver(new PrepareDevices());
	return new MobileResult(curStep, ResultStatus.OK, result);
    }

    private StepResult askForPUK(Step curStep) throws InterruptedException {
	interaction.requestCardInsertion();
	Promise<List<OutputInfoUnit>> waitForPUK = new Promise<>();
	interaction.onPinBlocked(new ConfirmPasswordOperationPINMgmtImpl(waitForPUK, GenericPINStep.PUK_FIELD));

	List<OutputInfoUnit> result = waitForPUK.deref();
	this.dispatcher.safeDeliver(new PrepareDevices());
	return new MobileResult(curStep, ResultStatus.OK, result);
    }

    private StepResult nextInt(Step curStep) throws InterruptedException {
	idx++;

	if (!(curStep instanceof GenericPINStep)) {
	    LOG.debug("nextINTswitch: return");
	    return new MobileResult(curStep, ResultStatus.OK, Collections.EMPTY_LIST);
	} else {
	    GenericPINStep genPINStp = (GenericPINStep) curStep;
	    RecognizedState recPinState = genPINStp.getPinState();

	    this.dispatcher.safeDeliver(new PowerDownDevices());

	    switch (recPinState) {
		case PIN_activated_RC3:
		    return askForPIN(curStep, 2);
		case PIN_activated_RC2:
		    return askForPIN(curStep, 1);
		case PIN_suspended:
		    return askForCAN(curStep);
		case PIN_resumed:
		    return askForPIN(curStep, 1);
		case PIN_blocked:
		    return askForPUK(curStep);
		case PIN_deactivated:
		    return new MobileResult(curStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
		case PUK_blocked:
		    return new MobileResult(curStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
		case UNKNOWN:
		    LOG.debug("nextINTswitch: UNKNOWN");
		    return new MobileResult(curStep, ResultStatus.OK, Collections.EMPTY_LIST);
		default:
		    LOG.debug("nextINTswitch: default");
		    return new MobileResult(curStep, ResultStatus.OK, Collections.EMPTY_LIST);
	    }
	}
    }

    @Override
    public StepResult previous() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replaceCurrent(Step step) {
	steps.set(idx, step);
	return current();
    }

    @Override
    public StepResult replaceNext(Step step) {
	steps.set(idx+1, step);
	return next();
    }

    @Override
    public StepResult replacePrevious(Step step) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRunningAction(Future<?> action) {
	// don't care about the action
    }

    @Override
    public void close() {
    }

    private void writeBackValues(List<InputInfoUnit> inInfo, List<OutputInfoUnit> outInfo) {
	for (InputInfoUnit infoInUnit : inInfo) {
	    for (OutputInfoUnit infoOutUnit : outInfo) {
		if (infoInUnit.getID().equals(infoOutUnit.getID())) {
		    infoInUnit.copyContentFrom(infoOutUnit);
		}
	    }
	}
    }



//    public List<OutputInfoUnit> getPinResult(Step step) throws InterruptedException {
//	// read values
//	String oldPinValue = this.userPinOld.deref();
//	String newPinValue = this.userPinNew.deref();
//	String canValue = this.userCan.deref();
//	String pukValue = this.userPuk.deref();
//
//	if (step instanceof GenericPINStep) {
//	    ArrayList<OutputInfoUnit> result = new ArrayList<>();
//	    for (InputInfoUnit nextIn : step.getInputInfoUnits()) {
//		if (oldPinValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("OLD_PIN_FIELD")) {
//		    PasswordField pw = new PasswordField(nextIn.getID());
//		    pw.copyContentFrom(nextIn);
//		    pw.setValue(oldPinValue.toCharArray());
//		    result.add(pw);
//		} else if (newPinValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("NEW_PIN_FIELD")) {
//		    PasswordField pw = new PasswordField(nextIn.getID());
//		    pw.copyContentFrom(nextIn);
//		    pw.setValue(newPinValue.toCharArray());
//		    result.add(pw);
//		} else if (newPinValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("NEW_PIN_REPEAT_FIELD")) {
//		    PasswordField pw = new PasswordField(nextIn.getID());
//		    pw.copyContentFrom(nextIn);
//		    pw.setValue(newPinValue.toCharArray());
//		    result.add(pw);
//		} else if (canValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("CAN_FIELD")) {
//		    PasswordField pw = new PasswordField(nextIn.getID());
//		    pw.copyContentFrom(nextIn);
//		    pw.setValue(canValue.toCharArray());
//		    result.add(pw);
//		} else if (pukValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("PUK_FIELD")) {
//		    PasswordField pw = new PasswordField(nextIn.getID());
//		    pw.copyContentFrom(nextIn);
//		    pw.setValue(pukValue.toCharArray());
//		    result.add(pw);
//		}
//	    }
//
//	    return result;
//	} else {
//	    throw new InterruptedException("The given step is not a PinStep.");
//	}
//    }
//
//    public void sendPinStatus(RecognizedState status) {
//
//	if (this.pinStatus.isDelivered()) {
//	   this.pinStatus = new Promise<>();
//	}
//
//	switch (status) {
//	    case PIN_activated_RC3:
//		this.pinStatus.deliver(PinStatus.RC3);
//		break;
//	    case PIN_activated_RC2:
//		this.pinStatus.deliver(PinStatus.RC2);
//		break;
//	    case PIN_suspended:
//		this.pinStatus.deliver(PinStatus.CAN);
//		break;
//	    case PIN_resumed:
//		this.pinStatus.deliver(PinStatus.RC1);
//		break;
//	    case PIN_blocked:
//		this.pinStatus.deliver(PinStatus.PIN_BLOCKED);
//		break;
//	    case PUK_blocked:
//		this.pinStatus.deliver(PinStatus.PUK_BLOCKED);
//		break;
//	    case PIN_deactivated:
//		this.pinStatus.deliver(PinStatus.DEACTIVATED);
//		break;
//	    default:
//		throw new IllegalArgumentException("Unhandled PIN status received from UI.");
//	}
//    }

}
