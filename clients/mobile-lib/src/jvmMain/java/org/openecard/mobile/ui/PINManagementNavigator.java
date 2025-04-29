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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.PowerDownDevices;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherExceptionUnchecked;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.common.util.Promise;
import org.openecard.common.util.SysUtils;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.mobile.activation.PinManagementInteraction;
import org.openecard.plugins.pinplugin.RecognizedState;
import org.openecard.plugins.pinplugin.gui.GenericPINAction;
import org.openecard.plugins.pinplugin.gui.GenericPINStep;
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
    private volatile Thread pMgmtNextThread;
    private final Dispatcher dispatcher;
    private final EventDispatcher eventDispatcher;
    private String tempCurrentPin = null;
    private String tempNewPin = null;
    private StepPostProcessor stepCleanup = null;


    public PINManagementNavigator(UserConsentDescription uc,
	    PinManagementInteraction interaction,
	    Dispatcher dispatcher,
	    EventDispatcher eventDispatcher) {
	this.steps = new ArrayList<>(uc.getSteps());
	this.interaction = interaction;
	this.dispatcher = dispatcher;
	this.eventDispatcher = eventDispatcher;
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
	    LOG.debug("Step: {}", s.description);
	}

	// TODO: remove this statement and implement it properly
//	return new MobileResult(pinStep, ResultStatus.INTERRUPTED, Collections.EMPTY_LIST);
	FutureTask<StepResult> pMgmtNext = new FutureTask<>(() -> nextInt(curStep));
	Thread nextThread = new Thread(pMgmtNext, "PIN-Mgmt-Next");
	pMgmtNextThread = nextThread;
	try {
	    // run next in thread and wait for completion
	    // note that promise does not allow to access the result in case of a cancellation#
	    nextThread.start();
	    LOG.debug("Waiting for next GUI step to finish.");
	    nextThread.join();
	    LOG.debug("Next GUI step finished.");
	    return pMgmtNext.get();
	} catch (InterruptedException ex) {
	    LOG.debug("Waiting for next GUI step interrupted, interrupting the GUI step processing.");
	    nextThread.interrupt();
	    try {
		// wait again after interrupting the thread
		LOG.debug("Waiting again for next GUI step to finish.");
		nextThread.join();
		LOG.debug("Next GUI step finished.");
		return pMgmtNext.get();
	    } catch (InterruptedException exIn) {
		return new MobileResult(curStep, ResultStatus.INTERRUPTED, Collections.emptyList());
	    } catch (ExecutionException exIn) {
		return evaluateExecutionException(exIn, exIn, curStep);
	    }
	} catch (ExecutionException ex) {
	    LOG.debug("Unexpected exception occurred in UI Step.", ex);
	    return new MobileResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
	} finally {
	    if (pMgmtNextThread == nextThread) {
		pMgmtNextThread = null;
	    }
	}
    }

    private StepResult askForPIN(GenericPINStep curStep, int attempt) throws InterruptedException {
	List<EventCallback> hooks = pauseExecution(curStep.getConHandle());

	Promise<List<OutputInfoUnit>> waitForPIN = new Promise<>();
	interaction.onPinChangeable(attempt, new ConfirmOldSetNewPasswordOperationPINMgmtImpl(waitForPIN));

	return createResult(waitForPIN, curStep, hooks);
    }

    private StepResult askForPIN(GenericPINStep curStep) throws InterruptedException {
	List<EventCallback> hooks = pauseExecution(curStep.getConHandle());

	Promise<List<OutputInfoUnit>> waitForPIN = new Promise<>();
	interaction.onPinChangeable(new ConfirmOldSetNewPasswordOperationPINMgmtImpl(waitForPIN));

	return createResult(waitForPIN, curStep, hooks);
    }

    private StepResult askForPinCanNewPin(GenericPINStep curStep) throws InterruptedException {
	this.stepCleanup = (Step step) -> {
	    if (curStep != step || curStep.getPinState() != RecognizedState.PIN_resumed) {
		tempCurrentPin = null;
		tempNewPin = null;
	    }
	};
	List<EventCallback> hooks = pauseExecution(curStep.getConHandle());

	Promise<PinCanNewPinContainer> waitForPinCan = new Promise<>();
	interaction.onPinCanNewPinRequired(new ConfirmPinCanNewPinPINMgmtImpl(waitForPinCan));

	PinCanNewPinContainer password = waitForPinCan.deref();
	List<OutputInfoUnit> lst = new ArrayList<>();
	PasswordField canField = new PasswordField(GenericPINStep.CAN_FIELD);
	final String can = password.getCan();
	if (can != null) {
	    canField.setValue(can.toCharArray());
	}
	lst.add(canField);

	this.tempCurrentPin = password.getCurrentPin();
	this.tempNewPin = password.getNewPin();

	return createResult(lst, curStep, hooks);
    }

    private StepResult askForPUK(GenericPINStep curStep) throws InterruptedException {
	List<EventCallback> hooks = pauseExecution(curStep.getConHandle());

	Promise<List<OutputInfoUnit>> waitForPUK = new Promise<>();
	interaction.onPinBlocked(new ConfirmPasswordOperationPINMgmtImpl(waitForPUK, GenericPINStep.PUK_FIELD));

	return createResult(waitForPUK, curStep, hooks);
    }

    private List<EventCallback> pauseExecution(ConnectionHandleType connectionHandle)  throws DispatcherExceptionUnchecked, InvocationTargetExceptionUnchecked {
	if (SysUtils.isAndroid()) {
	    EventCallback callback = new EventCallback() {
		@Override
		public void signalEvent(EventType eventType, EventObject eventData) {
		    if (eventType == EventType.CARD_REMOVED) {
			powerDownDevices(connectionHandle);
		    }
		}
	    };
	    this.eventDispatcher.add(callback, EventType.CARD_REMOVED);
	    List<EventCallback> results = new LinkedList<>();
	    results.add(callback);
	    return results;
	} else {
	    powerDownDevices(connectionHandle);
	    return Collections.EMPTY_LIST;
	}
    }

    private void powerDownDevices(ConnectionHandleType connectionHandle) throws DispatcherExceptionUnchecked, InvocationTargetExceptionUnchecked {
	PowerDownDevices pdd = new PowerDownDevices();
	pdd.setContextHandle(connectionHandle.getContextHandle());
	this.dispatcher.safeDeliver(pdd);
    }

    private void powerDownDevices(GenericPINStep curStep) throws DispatcherExceptionUnchecked, InvocationTargetExceptionUnchecked {
	powerDownDevices(curStep.getConHandle());
    }

    private StepResult notifyDeactivated(GenericPINStep curStep) {
	powerDownDevices(curStep);

	interaction.onCardDeactivated();

	return new MobileResult(curStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
    }
    private StepResult notifyPukBlocked(GenericPINStep curStep) {
	powerDownDevices(curStep);

	interaction.onCardPukBlocked();

	return new MobileResult(curStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
    }

    private StepResult createResult(Promise<List<OutputInfoUnit>> wait,
	    GenericPINStep curStep,
	    List<EventCallback> temporaryHooks) throws DispatcherExceptionUnchecked, InvocationTargetExceptionUnchecked, InterruptedException {
	List<OutputInfoUnit> result = wait.deref();
	return createResult(wait.deref(), curStep, temporaryHooks);
    }

    private StepResult createResult(List<OutputInfoUnit> result,
	    GenericPINStep curStep,
	    List<EventCallback> temporaryHooks) throws DispatcherExceptionUnchecked, InvocationTargetExceptionUnchecked, InterruptedException {
	for (EventCallback hook : temporaryHooks) {
	    this.eventDispatcher.del(hook);
	}
	return new MobileResult(curStep, ResultStatus.OK, result);
    }

    private StepResult nextInt(Step curStep) throws InterruptedException, Exception {
	idx++;
	if (stepCleanup != null) {
	    stepCleanup.process(curStep);
	    stepCleanup = null;
	}
	if (!(curStep instanceof GenericPINStep)) {
	    LOG.debug("nextINTswitch: return");
	    if (GenericPINAction.ERROR_STEP_ID.equals(curStep.getID())) {
		return new MobileResult(curStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
	    } else {
		return new MobileResult(curStep, ResultStatus.OK, Collections.EMPTY_LIST);
	    }
	} else {
	    GenericPINStep genPINStp = (GenericPINStep) curStep;
	    RecognizedState recPinState = genPINStp.getPinState();

	    switch (recPinState) {
		case PIN_activated_RC3:
		    return askForPIN(genPINStp, 3);
		case PIN_activated_RC2:
		    return askForPIN(genPINStp, 2);
		case PIN_suspended:
		    return askForPinCanNewPin(genPINStp);
		case PIN_resumed:
		    final char[] oldPinChars = this.tempCurrentPin == null ? new char[0] : this.tempCurrentPin.toCharArray();
		    final char[] newPinChars = this.tempNewPin == null ? new char[0] : this.tempNewPin.toCharArray();
		    this.tempCurrentPin = null;
		    this.tempNewPin = null;
		    List<OutputInfoUnit> lst = new ArrayList<>();
		    PasswordField newPinField = new PasswordField(GenericPINStep.NEW_PIN_FIELD);
		    newPinField.setValue(newPinChars);
		    lst.add(newPinField);
		    PasswordField newPinRepeat = new PasswordField(GenericPINStep.NEW_PIN_REPEAT_FIELD);
		    newPinRepeat.setValue(newPinChars);
		    lst.add(newPinRepeat);
		    PasswordField oldPinField = new PasswordField(GenericPINStep.OLD_PIN_FIELD);
		    oldPinField.setValue(oldPinChars);
		    lst.add(oldPinField);
		    return new MobileResult(genPINStp, ResultStatus.OK, lst);
		case PIN_blocked:
		    return askForPUK(genPINStp);
		case PIN_deactivated:
		    return notifyDeactivated(genPINStp);
		case PUK_blocked:
		    return notifyPukBlocked(genPINStp);
		case UNKNOWN:
		    LOG.debug("nextINTswitch: UNKNOWN");
		    genPINStp.generateGuiPinActivatedRc3();
		    return askForPIN(genPINStp);
		default:
		    LOG.debug("nextINTswitch: default");
		    return new MobileResult(genPINStp, ResultStatus.CANCEL, Collections.EMPTY_LIST);
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

}
