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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.util.Promise;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.mobile.activation.ConfirmAttributeSelectionOperation;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.ConfirmTwoPasswordsOperation;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.activation.SelectableItem;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.openecard.sal.protocol.eac.anytype.PasswordID;
import org.openecard.sal.protocol.eac.gui.CHATStep;
import org.openecard.sal.protocol.eac.gui.CVCStep;
import org.openecard.sal.protocol.eac.gui.ErrorStep;
import org.openecard.sal.protocol.eac.gui.PINStep;
import org.openecard.sal.protocol.eac.gui.PinState;
import org.openecard.sal.protocol.eac.gui.ProcessingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public final class EacNavigator extends MobileNavigator {

    private static final Logger LOG = LoggerFactory.getLogger(EacNavigator.class);

    private final List<Step> steps;
    private final EacInteraction interaction;

    private Future<?> runningAction;
    private Thread eacNextThread;

    private int idx = 0;
    private boolean pinFirstUse = true;


    public EacNavigator(UserConsentDescription uc, EacInteraction interaction) {
	this.steps = new ArrayList<>(uc.getSteps());
	this.interaction = interaction;
    }

    @Override
    public boolean hasNext() {
	return idx < steps.size();
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
	// get current step
	Step curStep = steps.get(idx);


	FutureTask<StepResult> eacNext = new FutureTask<>(() -> nextInt(curStep));
	try {
	    // run next in thread and wait for completion
	    // note that promise does not allow to access the result in case of a cancellation
	    eacNextThread = new Thread(eacNext, "EAC-GUI-Next");
	    eacNextThread.start();
	    LOG.debug("Waiting for next GUI step to finish.");
	    eacNextThread.join();
	    LOG.debug("Next GUI step finished.");
	    return eacNext.get();
	} catch (InterruptedException ex) {
	    LOG.debug("Waiting for next GUI step interrupted, interrupting the GUI step processing.");
	    eacNextThread.interrupt();
	    try {
		// wait again after interrupting the thread
		LOG.debug("Waiting again for next GUI step to finish.");
		eacNextThread.join();
		LOG.debug("Next GUI step finished.");
		return eacNext.get();
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
	    eacNextThread = null;
	}
    }

    private StepResult nextInt(Step curStep) {
	// handle step display
	if (CVCStep.STEP_ID.equals(curStep.getID())) {
	    idx++;
	    // step over CVC step, its data is processed in the next step
	    return new MobileResult(curStep, ResultStatus.OK, Collections.emptyList());
	} else if (CHATStep.STEP_ID.equals(curStep.getID())) {
	    idx++;
	    Step cvcStep = steps.get(0);
	    Step chatStep = steps.get(1);

	    return displayAndExecuteBackground(chatStep, () -> {
		ServerDataImpl sd = new ServerDataImpl(cvcStep, chatStep);
		String tInfo = getTransactionInfo();

		final Promise<List<OutputInfoUnit>> waitForAttributes = new Promise<>();
		ConfirmAttributeSelectionOperation selectionConfirmation = new ConfirmAttributeSelectionOperation() {
		    @Override
		    public void enter(List<SelectableItem> readAttr, List<SelectableItem> writeAttr) {
			List<OutputInfoUnit> outInfo = sd.getSelection(readAttr, writeAttr);
			waitForAttributes.deliver(outInfo);
		    }
		};

		try {
		    interaction.onServerData(sd, tInfo, selectionConfirmation);
		    List<OutputInfoUnit> outInfo = waitForAttributes.deref();
		    return new MobileResult(chatStep, ResultStatus.OK, outInfo);
		} catch (InterruptedException ex) {
		    return new MobileResult(cvcStep, ResultStatus.INTERRUPTED, Collections.emptyList());
		}
	    });
	} else if (PINStep.STEP_ID.equals(curStep.getID())) {
	    idx++;
	    Step pinStep = curStep;

	    return displayAndExecuteBackground(pinStep, () -> {
		if (pinFirstUse) {
		    pinFirstUse = false;
		} else {
		    // tell user the card is not needed anymore prior to capturing the pin again
		    interaction.onCardInteractionComplete();
		}

		EACData eacData = (EACData) DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY).get(EACProtocol.EAC_DATA);
		boolean isCanStep = eacData.pinID == PasswordID.CAN.getByte();

		final Promise<List<OutputInfoUnit>> waitForPin = new Promise<>();
		PinState ps = (PinState) DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY).get(TR03112Keys.NPA_PIN_STATE);
		if (ps == null) {
		    LOG.error("Missing PinState object.");
		    return new MobileResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
		} else if (! isCanStep && ps.isRequestCan()) {
		    interaction.onPinCanRequest(new ConfirmTwoPasswordsOperation() {
			@Override
			public void enter(String can, String pin) {
			    interaction.requestCardInsertion();
			    List<OutputInfoUnit> outInfo = getPinResult(pinStep, pin, can);
			    writeBackValues(pinStep.getInputInfoUnits(), outInfo);
			    waitForPin.deliver(outInfo);
			}
		    });
		} else {
		    ConfirmPasswordOperation op = new ConfirmPasswordOperation() {
			@Override
			public void enter(String pin) {
			    interaction.requestCardInsertion();
			    List<OutputInfoUnit> outInfo = getPinResult(pinStep, pin, null);
			    writeBackValues(pinStep.getInputInfoUnits(), outInfo);
			    waitForPin.deliver(outInfo);
			}
		    };
		    if (isCanStep) {
			interaction.onCanRequest(op);
		    } else {
			interaction.onPinRequest(ps.getAttempts(), op);
		    }
		}

		List<OutputInfoUnit> outInfo = waitForPin.deref();
		return new MobileResult(pinStep, ResultStatus.OK, outInfo);
	    });

	} else if (ProcessingStep.STEP_ID.equals(curStep.getID())) {
	    idx++;

	    return displayAndExecuteBackground(curStep, () -> {
		LOG.debug("Delivering final PIN status in ProcessingStep.");
		interaction.onCardAuthenticationSuccessful();
		return new MobileResult(curStep, ResultStatus.OK, Collections.emptyList());
	    });
	} else if (ErrorStep.STEP_ID.equals(curStep.getID())) {
	    idx++;

	    return displayAndExecuteBackground(curStep, () -> {
		PinState ps = (PinState) DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY).get(TR03112Keys.NPA_PIN_STATE);
		if (ps == null) {
		    LOG.error("Missing PinState object.");
		    return new MobileResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
		} else {
		    if (ps.isBlocked()) {
			interaction.onCardBlocked();
		    } else if (ps.isDeactivated()) {
			interaction.onCardDeactivated();
		    }
		}
		// TODO: check if this is needed here, we might tell the user that the card is not needed anymore at some other place
		interaction.onCardInteractionComplete();

		// cancel is returned by the step action
		return new MobileResult(curStep, ResultStatus.OK, Collections.emptyList());
	    });
	} else {
	    idx++;
	    return new MobileResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
	}
    }

    @Override
    public StepResult previous() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replaceCurrent(Step step) {
	steps.set(idx - 1, step);
	return current();
    }

    @Override
    public StepResult replaceNext(Step step) {
	steps.set(idx, step);
	return next();
    }

    @Override
    public StepResult replacePrevious(Step step) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRunningAction(Future<?> action) {
	this.runningAction = action;
    }

    @Override
    public void close() {
    }

    @Override
    protected StepResult displayAndExecuteBackground(Step stepObj, Callable<StepResult> step) {
	StepResult r = super.displayAndExecuteBackground(stepObj, step);
	return r;
    }

    public synchronized void cancel() {
	Thread curNext = eacNextThread;
	if (curNext != null) {
	    LOG.debug("Cancelling step display.");
	    curNext.interrupt();
	}

	// cancel a potentially running action
	Future<?> a = runningAction;
	if (a != null && ! a.isDone()) {
	    LOG.debug("Cancelling step action.");
	    a.cancel(true);
	}
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

    private String getTransactionInfo() {
	DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	EACData eacData = (EACData) ctx.get(EACProtocol.EAC_DATA);
	String tInfo = null;
	if (eacData != null) {
	    tInfo = eacData.transactionInfo;
	}
	return tInfo;
    }

    public List<OutputInfoUnit> getPinResult(Step step, String pinValue, String canValue) {
	PINStep pinStep = (PINStep) step;

	ArrayList<OutputInfoUnit> result = new ArrayList<>();
	for (InputInfoUnit nextIn : pinStep.getInputInfoUnits()) {
	    if (pinValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("PACE_PIN_FIELD")) {
		PasswordField pw = new PasswordField(nextIn.getID());
		pw.copyContentFrom(nextIn);
		pw.setValue(pinValue.toCharArray());
		result.add(pw);
	    } else if (canValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("PACE_CAN_FIELD")) {
		PasswordField pw = new PasswordField(nextIn.getID());
		pw.copyContentFrom(nextIn);
		pw.setValue(canValue.toCharArray());
		result.add(pw);
	    }
	}

	return result;
    }

}
