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

import java.io.InputStream;
import org.openecard.client.common.util.FileUtils;
import org.openecard.client.control.module.tctoken.hacks.ObjectTag;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenConverterTest {

    @Test(enabled = true)
    public void testConvert() {
	try {
	    InputStream testFileStream = getClass().getResourceAsStream("/TCTokenObject.xml");
	    String content = FileUtils.toString(testFileStream);

	    String data = ObjectTag.fix(content);

	    assertEquals(data, "<TCTokenType><ServerAddress>fry.mtg.de:443</ServerAddress><SessionIdentifier>0B01699AC700B72C152DE59479C46D98020A97E68DD8BB18476C539A25497644</SessionIdentifier><Binding>urn:liberty:paos:2006-08</Binding><PathSecurity-Protocol>urn:ietf:rfc:4279</PathSecurity-Protocol><PathSecurity-Parameters><PSK>31C22F3B0778E64FB3425F768C2C881E5F60236F7717259FAECE10CE20B928A4</PSK></PathSecurity-Parameters><RefreshAddress>https://willow.mtg.de:443/eid-server-demo-app/result/response.html</RefreshAddress></TCTokenType>");
	} catch (Exception e) {
	    fail(e.getMessage());
	}
    }

}
