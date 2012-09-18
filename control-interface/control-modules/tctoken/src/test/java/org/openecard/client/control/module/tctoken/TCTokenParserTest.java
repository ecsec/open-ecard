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

import generated.TCTokenType;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import org.openecard.client.common.util.FileUtils;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.control.module.tctoken.hacks.PathSecurityParameters;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenParserTest {

    @Test(enabled = !true)
    public void testParse() throws Exception {
	URL testFileLocation = getClass().getResource("/TCToken.xml");
	File testFile = new File(testFileLocation.toURI());

	TCTokenParser parser = new TCTokenParser();
	List<TCTokenType> tokens = parser.parse(new FileInputStream(testFile));

	TCTokenType t = tokens.get(0);
	assertEquals(t.getSessionIdentifier(), "3eab1b41ecc1ce5246acf6f4e275");
	assertEquals(t.getServerAddress().toString(), "https://eid-ref.my-service.de:443");
	assertEquals(t.getRefreshAddress().toString(), "https://eid.services.my.net:443/?sessionID=D9D6851A7C02167A5699DA57657664715F4D9C44E50A94F7A83909D24AFA997A");
	assertEquals(t.getBinding(), "urn:liberty:paos:2006-08");
    }

    @Test
    public void testParseMalformed() throws Exception {
	URL testFileLocation = getClass().getResource("/TCToken-malformed.xml");
	String data = FileUtils.toString(testFileLocation.openStream());

	data = PathSecurityParameters.fix(data);

	TCTokenParser parser = new TCTokenParser();
	List<TCTokenType> tokens = parser.parse(data);

	TCTokenType t = tokens.get(0);
	assertEquals(t.getSessionIdentifier(), "3eab1b41ecc1ce5246acf6f4e275");
	assertEquals(t.getServerAddress().toString(), "https://eid-ref.my-service.de:443");
	assertEquals(t.getRefreshAddress().toString(), "https://eid.services.my.net:443/?sessionID=D9D6851A7C02167A5699DA57657664715F4D9C44E50A94F7A83909D24AFA997A");
	assertEquals(t.getBinding(), "urn:liberty:paos:2006-08");
	assertEquals(t.getPathSecurityParameters().getPSK(), StringUtils.toByteArray("b7e9dd2ba2568c3c8d572aaadb3eebf7d4515e66d5fc2fd8e46626725a9abba2"));
    }

}
