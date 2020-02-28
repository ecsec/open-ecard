/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.testng.annotations.Test;
import skid.mob.lib.Cancellable;


/**
 *
 * @author Tobias Wich
 */
public class SamlClientTest {

    @Test
    public void testFetchSession() throws InterruptedException, ExecutionException {
	SamlClientImpl client = new SamlClientImpl();
	CompletableFuture<String> cb = new CompletableFuture<>();
	Cancellable c = client.startSession("https://lif-test.fifty-fifty.taxi/fifty-fifty-service/api/oauth/authorize?response_type=token&client_id=fifty-fifty&scope=auth&state=",
		v -> {cb.complete("init");}, (v1, v2) -> {cb.complete("error");}, v -> {cb.complete("finished");});
	//Thread.sleep(200);
	//c.cancel();
	cb.get();
    }

}
