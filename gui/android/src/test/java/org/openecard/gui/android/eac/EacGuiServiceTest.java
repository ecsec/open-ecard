/*
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
 */
package org.openecard.gui.android.eac;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import mockit.Mocked;
import mockit.Tested;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;

/**
 *
 * @author Neil Crossley
 */
public class EacGuiServiceTest {

    @Mocked
    EacGui.Stub stub;

    @Mocked
    Service service;

    @BeforeTest
    public void setUpSuite() {
	EacGuiService.prepare();
    }

    @AfterMethod
    public void tearDown() {
	EacGuiService.close();
    }

    @Test
    public void canAssignGuiSuccessfully(@Mocked EacGuiImpl input) {
	EacGuiService.setGuiImpl(input);
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void cannotDeliverGuiMultipleTimes(@Mocked EacGuiImpl input1, @Mocked EacGuiImpl input2) throws IllegalStateException {
	EacGuiService.setGuiImpl(input1);
	EacGuiService.setGuiImpl(input2);
    }

    @Test
    public void givenGuiIsNotAssignedThenBindingWaitsForever(@Tested final EacGuiService sut, @Mocked final Intent input) {
	Future<IBinder> future = callBindAsync(sut, input);
	try {
	    //wait 1 second for the task to complete.
	    future.get(1000, TimeUnit.MILLISECONDS);

	    Assert.fail("The call to onBind(...) is supposed to wait forever and not terminate.");
	} catch (InterruptedException | ExecutionException | TimeoutException e) {
	    // Pass
	}
    }

    @Test
    public void givenBindingStartsBeforeGuiAssignmentThenBindingWaitsForGuiAssignment(@Tested final EacGuiService sut,
	    @Mocked final Intent inputIntent,
	    @Mocked EacGuiImpl inputGui) throws InterruptedException, ExecutionException, TimeoutException {

	Future<IBinder> future = callBindAsync(sut, inputIntent);
	TimeUnit.MILLISECONDS.sleep(2);
	EacGuiService.setGuiImpl(inputGui);
	IBinder result = getValueImmediately(future);

	assertEquals(result, inputGui);
    }
    
    @Test
    public void givenGuiIsAssignedThenCanBindMultipleTimes(
	    @Tested final EacGuiService sut,
	    @Mocked final Intent inputIntent,
	    @Mocked EacGuiImpl inputGui) throws InterruptedException, ExecutionException, TimeoutException {
	
	EacGuiService.setGuiImpl(inputGui);
	
	IBinder result1 = getValueImmediately(sut, inputIntent);
	IBinder result2 = getValueImmediately(sut, inputIntent);
	IBinder result3 = getValueImmediately(sut, inputIntent);

	assertEquals(inputGui, result1);
	assertEquals(inputGui, result2);
	assertEquals(inputGui, result3);
    }

    @Test
    public void givenClientIsWaitingWhenGuiIsPreparedAndAssignedThenClientResolves(
	    @Tested final EacGuiService sut,
	    @Mocked final Intent inputIntent,
	    @Mocked EacGuiImpl inputGui) throws InterruptedException, ExecutionException, TimeoutException {
	
	Future<IBinder> waitingClient = callBindAsync(sut, inputIntent);
	startWaiting(waitingClient);
	
	EacGuiService.prepare();
	EacGuiService.setGuiImpl(inputGui);
	
	IBinder result = getValueImmediately(waitingClient);

	assertEquals(inputGui, result);
    }

    private IBinder getValueImmediately(final EacGuiService sut, final Intent inputIntent) throws TimeoutException, InterruptedException, ExecutionException {
	Future<IBinder> future = callBindAsync(sut, inputIntent);
	IBinder result = getValueImmediately(future);
	return result;
    }
    
    private void startWaiting(Future<IBinder> future) throws InterruptedException, ExecutionException {
	try {
	    IBinder result = future.get(1, TimeUnit.MILLISECONDS);
	    assertNull(result, "If futures should suddenly resolve timeouts without throwing exceptions, we still don't expect a return value!");
	} catch (TimeoutException ex) {
	    // This is expected
	}
    }
    
    /**
     * Call this utility function when a future is not expected to block.
     * 
     * If the code contains bugs, calling future.get() may block forever, 
     * halting the test suite. This method ensures that accessing the future 
     * will fail fast instead.
     * @param future The future that may block forever.
     * @return The binder.
     * @throws ExecutionException An unexpected exception.
     * @throws InterruptedException An unexpected exception.
     * @throws TimeoutException Thrown only if directly accessing the future 
     *	    blocks unexpectedly.
     */
    private IBinder getValueImmediately(Future<IBinder> future) throws ExecutionException, InterruptedException, TimeoutException {
	IBinder result1 = future.get(1, TimeUnit.MILLISECONDS);
	return result1;
    }

    private Future<IBinder> callBindAsync(final EacGuiService sut, final Intent input) {
	ExecutorService exec = Executors.newSingleThreadExecutor();
	Future<IBinder> future = exec.submit(new Callable<IBinder>() {
	    @Override
	    public IBinder call() throws Exception {
		return sut.onBind(input);
	    }
	});
	exec.shutdown();
	return future;
    }
}
