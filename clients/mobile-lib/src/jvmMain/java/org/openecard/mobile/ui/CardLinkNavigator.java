/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.*;
import org.openecard.common.util.Promise;
import org.openecard.common.util.SysUtils;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.StepWithConnection;
import org.openecard.gui.definition.*;
import org.openecard.mobile.activation.*;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.sal.protocol.eac.gui.ErrorStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


/**
 *
 * @author Tobias Wich
 */
public final class CardLinkNavigator extends MobileNavigator {

	private static final Logger LOG = LoggerFactory.getLogger(CardLinkNavigator.class);

	private final List<Step> steps;
	private final CardLinkInteraction interaction;
	private final EventDispatcher eventDispatcher;

	private Future<?> runningAction;
	private volatile Thread cardLinkNextThread;

	private int idx = 0;
	private boolean pinFirstUse = true;

	private final NFCDialogMsgSetter msgSetter;
	private final Dispatcher dispatcher;

	public CardLinkNavigator(UserConsentDescription uc, CardLinkInteraction interaction, NFCDialogMsgSetter msgSetter,
							 Dispatcher dispatcher, EventDispatcher eventDispatcher ) {
		this.steps = new ArrayList<>(uc.getSteps());
		this.interaction = interaction;
		this.msgSetter = msgSetter;
		this.dispatcher = dispatcher;
		this.eventDispatcher = eventDispatcher;
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
		Thread nextThread = new Thread(eacNext, "CardLink-GUI-Next");
		cardLinkNextThread = nextThread;
		try {
			// run next in thread and wait for completion
			// note that promise does not allow to access the result in case of a cancellation
			nextThread.start();
			LOG.debug("Waiting for next GUI step to finish.");
			nextThread.join();
			LOG.debug("Next GUI step finished.");
			return eacNext.get();
		} catch (InterruptedException ex) {
			LOG.debug("Waiting for next GUI step interrupted, interrupting the GUI step processing.");
			nextThread.interrupt();
			try {
				// wait again after interrupting the thread
				LOG.debug("Waiting again for next GUI step to finish.");
				nextThread.join();
				LOG.debug("Next GUI step finished.");
				return eacNext.get();
			} catch (InterruptedException exIn) {
				return new MobileResult(curStep, ResultStatus.INTERRUPTED, Collections.emptyList());
			} catch (ExecutionException exIn) {
				return evaluateExecutionException(exIn, exIn, curStep);
			}
		} catch (ExecutionException ex) {
			LOG.error("Unexpected exception occurred in UI Step.", ex);
			return new MobileResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
		} finally {
			if (cardLinkNextThread == nextThread) {
				cardLinkNextThread = null;
			}

		}
	}

	private StepResult nextInt(Step curStep) {
		// handle step display
		// TODO: Use Step IDs from Cardlink Addon, maybe move them to Mobile Lib?
		if ("PROTOCOL_CARDLINK_GUI_STEP_PHONE".equals(curStep.getId())) {
			idx++;
			return displayAndExecuteBackground(curStep, () -> {
				final Promise<List<OutputInfoUnit>> waitForPhoneNumber = new Promise<>();
				final ConfirmTextOperation confirmTextOperation = new ConfirmCardLinkPhoneNumberImpl(waitForPhoneNumber, curStep, this);

				try {
					interaction.onPhoneNumberRequest(confirmTextOperation);
					List<OutputInfoUnit> phoneNumber = waitForPhoneNumber.deref();
					return new MobileResult(curStep, ResultStatus.OK, phoneNumber);
				} catch (InterruptedException ex) {
					return new MobileResult(curStep, ResultStatus.INTERRUPTED, Collections.emptyList());
				}
			});
		} else if ("PROTOCOL_CARDLINK_GUI_STEP_PHONE_RETRY".equals(curStep.getId())) {
			idx++;
			return displayAndExecuteBackground(curStep, () -> {
				final Promise<List<OutputInfoUnit>> waitForPhoneNumber = new Promise<>();
				final ConfirmTextOperation confirmTextOperation = new ConfirmCardLinkPhoneNumberImpl(waitForPhoneNumber, curStep, this);

				try {
					var dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
					var resultCode = (CardLinkErrorCodes.CardLinkCodes) dynCtx.get("CardLink::SERVICE_ERROR_CODE");
					var errorMessage = (String) dynCtx.get("CardLink::ERROR_MESSAGE");
					//we do handle these errors so, clean context to avoid state confusion in later steps
					dynCtx.remove("CardLink::SERVICE_ERROR_CODE");
					dynCtx.remove("CardLink::ERROR_MESSAGE");

					interaction.onPhoneNumberRetry(confirmTextOperation, resultCode.name(), errorMessage);
					List<OutputInfoUnit> phoneNumber = waitForPhoneNumber.deref();
					return new MobileResult(curStep, ResultStatus.OK, phoneNumber);
				} catch (InterruptedException ex) {
					return new MobileResult(curStep, ResultStatus.INTERRUPTED, Collections.emptyList());
				}
			});
		} else if ("PROTOCOL_CARDLINK_GUI_STEP_TAN".equals(curStep.getId())) {
			idx++;
			return displayAndExecuteBackground(curStep, () -> {
				final Promise<List<OutputInfoUnit>> waitForTan = new Promise<>();
				final ConfirmPasswordOperation confirmTan = new ConfirmCardLinkTanImpl(waitForTan, curStep, this);

				try {
					interaction.onSmsCodeRequest(confirmTan);
					List<OutputInfoUnit> tan = waitForTan.deref();
					return new MobileResult(curStep, ResultStatus.OK, tan);
				} catch (InterruptedException ex) {
					return new MobileResult(curStep, ResultStatus.INTERRUPTED, Collections.emptyList());
				}
			});
		} else if ("PROTOCOL_CARDLINK_GUI_STEP_TAN_RETRY".equals(curStep.getId())) {
			idx++;
			return displayAndExecuteBackground(curStep, () -> {
				final Promise<List<OutputInfoUnit>> waitForTan = new Promise<>();
				final ConfirmPasswordOperation confirmTan = new ConfirmCardLinkTanImpl(waitForTan, curStep, this);

				try {
					var dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
					var resultCode = (CardLinkErrorCodes.CardLinkCodes) dynCtx.get("CardLink::SERVICE_ERROR_CODE");
					var errorMessage = (String) dynCtx.get("CardLink::ERROR_MESSAGE");
					//we do handle these errors so, clean context to avoid state confusion in later steps
					dynCtx.remove("CardLink::SERVICE_ERROR_CODE");
					dynCtx.remove("CardLink::ERROR_MESSAGE");

					interaction.onSmsCodeRetry(confirmTan, resultCode.name(), errorMessage);
					List<OutputInfoUnit> tan = waitForTan.deref();
					return new MobileResult(curStep, ResultStatus.OK, tan);
				} catch (InterruptedException ex) {
					return new MobileResult(curStep, ResultStatus.INTERRUPTED, Collections.emptyList());
				}
			});
		} else if ("PROTOCOL_CARDLINK_GUI_STEP_DIRECT_CONNECT".equals(curStep.getId())) {
			idx++;
			interaction.requestCardInsertion();
			return new MobileResult(curStep, ResultStatus.OK, Collections.emptyList());
		} else if ("PROTOCOL_CARDLINK_GUI_STEP_ENTER_CAN".equals(curStep.getId())) {
			idx++;

			StepWithConnection canStep = (StepWithConnection) curStep;

			return displayAndExecuteBackground(canStep, () -> {
				List<EventCallback> hooks = pauseExecution(canStep.getConnectionHandle());

				final Promise<List<OutputInfoUnit>> waitForCan = new Promise<>();
				final ConfirmPasswordOperation confirmCan = new ConfirmCardLinkCanImpl(waitForCan, canStep, interaction,msgSetter,this);

				try {
					interaction.onCanRequest(confirmCan);
					List<OutputInfoUnit> tan = waitForCan.deref();
					for (EventCallback hook: hooks){
						this.eventDispatcher.del(hook);
					}
					return new MobileResult(canStep, ResultStatus.OK, tan);
				} catch (InterruptedException ex) {
					return new MobileResult(canStep, ResultStatus.INTERRUPTED, Collections.emptyList());
				}
			});
		} else if ("PROTOCOL_CARDLINK_GUI_STEP_ENTER_CAN_RETRY".equals(curStep.getId())) {
			idx++;

			StepWithConnection canStep = (StepWithConnection) curStep;

			return displayAndExecuteBackground(canStep, () -> {
				List<EventCallback> hooks = pauseExecution(canStep.getConnectionHandle());

				final Promise<List<OutputInfoUnit>> waitForCan = new Promise<>();
				final ConfirmPasswordOperation confirmCan = new ConfirmCardLinkCanImpl(waitForCan, canStep, interaction,msgSetter,this);

				try {
					var dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
					var resultCode = (CardLinkErrorCodes.ClientCodes) dynCtx.get("CardLink::CLIENT_ERROR_CODE");
					var errorMessage = (String) dynCtx.get("CardLink::ERROR_MESSAGE");

					//we do handle these errors so, clean context to avoid state confusion in later steps
					dynCtx.remove("CardLink::CLIENT_ERROR_CODE");
					dynCtx.remove("CardLink::ERROR_MESSAGE");

					interaction.onCanRetry(confirmCan, resultCode.name(), errorMessage);
					List<OutputInfoUnit> tan = waitForCan.deref();
					for (EventCallback hook: hooks){
						this.eventDispatcher.del(hook);
					}
					return new MobileResult(canStep, ResultStatus.OK, tan);
				} catch (InterruptedException ex) {
					return new MobileResult(canStep, ResultStatus.INTERRUPTED, Collections.emptyList());
				}
			});
		} else if (ErrorStep.STEP_ID.equals(curStep.getId())) {
			return new MobileResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
		} else {
			idx++;
			return new MobileResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
		}
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
		Thread curNext = cardLinkNextThread;
		if (curNext != null) {
			LOG.debug("Cancelling step display.");
			curNext.interrupt();
		}

		// cancel a potentially running action
		Future<?> a = runningAction;
		if (a != null && !a.isDone()) {
			LOG.debug("Cancelling step action.");
			a.cancel(true);
		}
	}

	public List<OutputInfoUnit> writeBackValues(Step step, String value) {
		ArrayList<OutputInfoUnit> result = new ArrayList<>();
		for (InputInfoUnit nextIn : step.getInputInfoUnits()) {
			// TODO: Use Step IDs from Cardlink Addon, maybe move them to Mobile Lib?
			if (value != null && nextIn instanceof TextField && nextIn.getId().equals("CARDLINK_FIELD_PHONE")) {
				TextField tf = new TextField(nextIn.getId());
				tf.copyContentFrom(nextIn);
				tf.setValue(value.toCharArray());
				result.add(tf);
			} else if (value != null && nextIn instanceof TextField && nextIn.getId().equals("CARDLINK_FIELD_TAN")) {
				TextField tf = new TextField(nextIn.getId());
				tf.copyContentFrom(nextIn);
				tf.setValue(value.toCharArray());
				result.add(tf);
			} else if (value != null && nextIn instanceof TextField && nextIn.getId().equals("CARDLINK_FIELD_CAN")) {
				TextField tf = new TextField(nextIn.getId());
				tf.copyContentFrom(nextIn);
				tf.setValue(value.toCharArray());
				result.add(tf);
			}
		}
		return result;
	}
}
