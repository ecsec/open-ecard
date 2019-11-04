/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.BackgroundTask;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.mobile.MobileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mobile base class for GUI Navigators.
 *
 * @author Tobias Wich
 */
public abstract class MobileNavigator implements UserConsentNavigator {

    private static final Logger LOG = LoggerFactory.getLogger(MobileNavigator.class);


    /**
     * Run background task in parallel to logic driving the UI data exchange.
     * The background task is taken from the step object.
     *
     * @param stepObj
     * @param step
     * @return
     */
    protected StepResult displayAndExecuteBackground(@Nonnull Step stepObj, @Nonnull Callable<StepResult> step) {
	AtomicInteger threadIdx = new AtomicInteger(1);
	ExecutorService executor = Executors.newFixedThreadPool(2, task -> {
	    return new Thread(task, String.format("MobileGUI-%d", threadIdx.getAndIncrement()));
	});

	// submit tasks
	CompletionService<StepResult> completionService = new ExecutorCompletionService<>(executor);
	Future<StepResult> stepThread = completionService.submit(step);
	Future<StepResult> backThread = null;
	BackgroundTask background = stepObj.getBackgroundTask();
	if (background != null) {
	    completionService.submit(convertResult(stepObj, background));
	}

	try {
	    // get first finished
	    Future<StepResult> finishedThread = completionService.take();
	    try {
		return finishedThread.get();
	    } catch (ExecutionException ex) {
		// errors in background are logged, but don't affect the outcome
		if (finishedThread == backThread) {
		    LOG.error("Error executing GUI background task.", ex.getCause());
		}
		// get the step result as background thread is out
		try {
		    return stepThread.get();
		} catch (ExecutionException ex2) {
		    LOG.error("Error executing UI task.", ex2);
		    return new MobileResult(stepObj, ResultStatus.CANCEL, Collections.emptyList());
		}
	    }
	} catch (InterruptedException ex) {
	    return new MobileResult(stepObj, ResultStatus.INTERRUPTED, Collections.emptyList());
	} finally {
	    executor.shutdownNow();
	    try {
		executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
	    } catch (InterruptedException ex) {
		return new MobileResult(stepObj, ResultStatus.INTERRUPTED, Collections.emptyList());
	    }
	}
    }

    private Callable<StepResult> convertResult(@Nonnull Step stepObj, @Nonnull Callable<StepActionResult> task) {
	return () -> {
	    StepActionResult sar = task.call();
	    ResultStatus r;
	    switch (sar.getStatus()) {
		case NEXT:
		    r = ResultStatus.OK;
		    break;
		case BACK:
		    r = ResultStatus.BACK;
		    break;
		case REPEAT:
		    r = ResultStatus.RELOAD;
		    break;
		case CANCEL:
		default:
		    r = ResultStatus.CANCEL;
	    }
	    return new MobileResult(stepObj, r, Collections.emptyList(), sar.getReplacement());
	};
    }

}
