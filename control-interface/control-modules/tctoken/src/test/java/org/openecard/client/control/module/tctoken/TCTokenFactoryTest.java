/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.control.module.tctoken;

import java.net.URL;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenFactoryTest {

    @Test
    public void testGenerateTCToken() throws Exception {
	try {
	    String url = "http://openecard-demo.vserver-001.urospace.de/tcToken?card-type=http://bsi.bund.de/cif/npa.xml";
	    URL tcTokenURL = new URL(url);
	    TCToken result = TCTokenFactory.generateTCToken(tcTokenURL);
	} catch (Exception e) {
	    Assert.fail(e.getMessage());
	}
    }

}
