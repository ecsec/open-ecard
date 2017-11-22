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
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.IBinder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.testng.Assert;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.AfterTest;
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
	EacGuiService.prepare();
    }
    
    @Test
    public void canDeliverCorrectGui(@Mocked EacGuiImpl input) {
	EacGuiService.setGuiImpl(input);
    }
    
    @Test(expectedExceptions = {IllegalStateException.class})
    public void canDeliverGuiMultipleTimes(@Mocked EacGuiImpl input1, @Mocked EacGuiImpl input2) throws IllegalStateException {
	EacGuiService.setGuiImpl(input1);
	EacGuiService.setGuiImpl(input2);
    }
    
    @Test
    public void GivenNoGuiImplSetThenBindingWaitsForever(@Tested final EacGuiService sut, @Mocked final Intent input)  {
	ExecutorService exec = Executors.newSingleThreadExecutor();
	Future<Integer> future = exec.submit(new Callable<Integer>(){
	    @Override
	    public Integer call() throws Exception {
		sut.onBind(input);

		return 0;
	    }
	});
	exec.shutdown();
	try {
	    //wait 5 seconds for the task to complete.
	    future.get(1000, TimeUnit.MILLISECONDS);
	    
	    Assert.fail("The call to onBind(...) is supposed to wait forever and not terminate.");
	}
	catch (InterruptedException | ExecutionException | TimeoutException e) {
	    // Pass
	}
	
    }
}
