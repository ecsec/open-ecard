/****************************************************************************
 * Copyright (C) 2016-2019 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import org.openecard.mdlw.sal.config.MiddlewareConfigLoader;
import org.openecard.mdlw.sal.config.MiddlewareSALConfig;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.openecard.common.ClientEnv;
import org.openecard.common.event.EventDispatcherImpl;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.mdlw.sal.exceptions.InitializationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 *
 * @author René Lottes
 */
public class TestEventManager {

    private MiddlewareSALConfig mwConfig;

    @BeforeClass
    public void init() throws IOException, FileNotFoundException, JAXBException {
        mwConfig = new MiddlewareConfigLoader().getMiddlewareSALConfigs().get(0);
    }

    @Test(enabled = false)
    public void test() throws InterruptedException, InitializationException {
	Environment env = new ClientEnv();
	MiddlewareSAL mwSAL = new MiddlewareSAL(env, mwConfig);
	env.setSAL(mwSAL);
	EventDispatcher ed = new EventDispatcherImpl();
	env.setEventDispatcher(ed);
	mwSAL.initialize(new Initialize());
	ed.start();

	Thread.sleep(10000);
    }

}
