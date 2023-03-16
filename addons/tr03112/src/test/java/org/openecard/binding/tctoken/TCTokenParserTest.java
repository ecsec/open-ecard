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

package org.openecard.binding.tctoken;

import generated.TCTokenType;
import java.io.InputStream;
import java.util.List;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.StringUtils;
import org.testng.annotations.Test;

import static org.openecard.common.ECardConstants.BINDING_PAOS;
import static org.testng.Assert.*;


/**
 * @author Moritz Horsch
 */
public class TCTokenParserTest {

    @Test(enabled = true)
    public void testParse() throws Exception {
	InputStream testFile = FileUtils.resolveResourceAsStream(getClass(), "TCToken.xml");

	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(testFile);

	TCTokenType t = tokens.get(0);
	assertEquals(t.getSessionIdentifier(), "3eab1b41ecc1ce5246acf6f4e2751234");
	assertEquals(t.getServerAddress().toString(), "https://eid-ref.my-service.de:443");
	assertEquals(t.getRefreshAddress().toString(), "https://eid.services.my.net:443/?sessionID=D9D6851A7C02167A5699DA57657664715F4D9C44E50A94F7A83909D24AFA997A");
	assertEquals(t.getBinding(), BINDING_PAOS);
    }

    @Test
    public void testParseMalformed() throws Exception {
	String data = FileUtils.toString(FileUtils.resolveResourceAsStream(getClass(), "TCToken-malformed.xml"));

	data = TCTokenHacks.fixPathSecurityParameters(data);

	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(data);

	TCTokenType t = tokens.get(0);
	assertEquals(t.getSessionIdentifier(), "3eab1b41ecc1ce5246acf6f4e275");
	assertEquals(t.getServerAddress().toString(), "https://eid-ref.my-service.de:443");
	assertEquals(t.getRefreshAddress().toString(), "https://eid.services.my.net:443/?sessionID=D9D6851A7C02167A5699DA57657664715F4D9C44E50A94F7A83909D24AFA997A");
	assertEquals(t.getBinding(), BINDING_PAOS);
	assertEquals(t.getPathSecurityParameters().getPSK(), StringUtils.toByteArray("b7e9dd2ba2568c3c8d572aaadb3eebf7d4515e66d5fc2fd8e46626725a9abba2"));
    }

}
