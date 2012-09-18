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
import org.testng.annotations.Test;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenVerifierTest {

    private TCTokenType token;
    private TCTokenVerifier verifier;

    public TCTokenVerifierTest() throws Exception {
	URL testFileLocation = TCTokenVerifierTest.class.getResource("/TCToken.xml");
	File testFile = new File(testFileLocation.toURI());

	TCTokenParser parser = new TCTokenParser();
	List<TCTokenType> tokens = parser.parse(new FileInputStream(testFile));
	token = tokens.get(0);
	verifier = new TCTokenVerifier(token);
    }

    @Test
    public void testVerify() throws Exception {
	verifier.verify();
    }

    @Test(expectedExceptions = TCTokenException.class)
    public void testVerifyServerAddress() throws Exception {
	token.setServerAddress(null);
	verifier.verifyServerAddress();
    }

    @Test(expectedExceptions = TCTokenException.class)
    public void testVerifySessionIdentifier() throws Exception {
	token.setSessionIdentifier("");
	verifier.verifySessionIdentifier();
    }

    @Test(expectedExceptions = TCTokenException.class)
    public void testVerifySessionIdentifier2() throws Exception {
	token.setSessionIdentifier("123456");
	verifier.verifySessionIdentifier();
    }

    @Test(expectedExceptions = TCTokenException.class)
    public void testVerifyRefreshAddress() throws Exception {
	token.setRefreshAddress(null);
	verifier.verifyRefreshAddress();
    }

    @Test(expectedExceptions = TCTokenException.class)
    public void testVerifyBinding() throws Exception {
	token.setBinding("urn:liberty:city:2006-08");
	verifier.verifyBinding();
    }

    @Test(expectedExceptions = TCTokenException.class)
    public void testVerifyPathSecurityProtocol() throws Exception {
	token.setPathSecurityProtocol("urn:ietf:rfc:42791");
	verifier.verifyPathSecurityProtocol();
    }

    @Test
    public void testVerifyPathSecurityParameters() throws Exception {
	token.setPathSecurityProtocol("urn:ietf:rfc:4279");
	token.setPathSecurityParameters(null);
	verifier.verifyPathSecurityParameters();
    }

    @Test(expectedExceptions = TCTokenException.class)
    public void testVerifyPathSecurityParameters2() throws Exception {
	TCTokenType.PathSecurityParameters psp = new TCTokenType.PathSecurityParameters();
	psp.setPSK(null);
	token.setPathSecurityParameters(psp);
	verifier.verifyPathSecurityParameters();
    }

}
