/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import java.io.IOException;
import java.net.URL;
import org.openecard.common.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class TCTokenFactoryTest {

    @Test
    public void testGenerateTCToken_String() throws Exception {
	String tcToken = FileUtils.toString(FileUtils.resolveResourceAsStream(TCTokenFactoryTest.class, "TCToken.xml"), "UTF-8");
	TCTokenContext result = TCTokenContext.generateTCToken(tcToken);
	Assert.assertNotNull(result.getToken(), "TCToken is null.");
    }

    @Test(expectedExceptions = {ResourceException.class})
    public void testGenerateTCToken_TCTokenType() throws TCTokenException, IOException, ResourceException,
	    ValidationError {
	URL tcTokenURL = FileUtils.resolveResourceAsURL(TCTokenFactoryTest.class, "TCToken.xml");
	// should fail, since a non-https-URL is used
	TCTokenContext.generateTCToken(tcTokenURL);
    }

}
