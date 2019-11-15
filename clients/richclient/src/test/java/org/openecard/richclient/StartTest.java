/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.richclient;

import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class StartTest {

    @Test
    public void startMain() throws InterruptedException {
	RichClient.main(new String[0]);
	    // Wait some seconds until the client comes up
	Thread.sleep(600000);
    }
}
