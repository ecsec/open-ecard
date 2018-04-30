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

import org.openecard.binding.tctoken.ex.InvalidTCTokenElement;
import org.openecard.binding.tctoken.ex.ActivationError;
import generated.TCTokenType;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.openecard.common.util.FileUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * @author Moritz Horsch
 */
public class TCTokenVerifierTest {

    private TCToken token;
    private TCTokenVerifier verifier;

    @BeforeTest
    public void initTestObject() throws Exception {
	InputStream testFile = FileUtils.resolveResourceAsStream(getClass(), "TCToken.xml");

	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(testFile);
	token = tokens.get(0);
	verifier = new TCTokenVerifier(token, new ResourceContext(null, null, Collections.EMPTY_LIST));
    }

    @Test
    public void testVerify() throws Exception {
	verifier.verifyUrlToken();
    }

    @Test(expectedExceptions = InvalidTCTokenElement.class)
    public void testVerifyServerAddress() throws ActivationError {
	token.setServerAddress(null);
	verifier.verifyServerAddress();
    }

    @Test(expectedExceptions = InvalidTCTokenElement.class)
    public void testVerifySessionIdentifier() throws ActivationError {
	token.setSessionIdentifier("");
	verifier.verifySessionIdentifier();
    }

    @Test(expectedExceptions = InvalidTCTokenElement.class)
    public void testVerifyRefreshAddress() throws ActivationError {
	token.setRefreshAddress(null);
	token.setCommunicationErrorAddress("https://localhost/error");
	verifier.verifyRefreshAddress();
    }

    @Test(expectedExceptions = InvalidTCTokenElement.class)
    public void testVerifyBinding() throws ActivationError {
	token.setBinding("urn:liberty:city:2006-08");
	verifier.verifyBinding();
    }

    @Test(expectedExceptions = InvalidTCTokenElement.class, enabled = false)
    public void testVerifyPathSecurityProtocol() throws ActivationError {
	token.setPathSecurityProtocol("urn:ietf:rfc:42791");
	verifier.verifyPathSecurity();
    }

    @Test(expectedExceptions = InvalidTCTokenElement.class, enabled = false)
    public void testVerifyPathSecurityParameters() throws ActivationError {
	token.setPathSecurityProtocol("urn:ietf:rfc:4279");
	token.setPathSecurityParameters(null);
	verifier.verifyPathSecurity();
    }

    @Test(expectedExceptions = InvalidTCTokenElement.class, enabled = false)
    public void testVerifyPathSecurityParameters2() throws ActivationError {
	TCTokenType.PathSecurityParameters psp = new TCTokenType.PathSecurityParameters();
	psp.setPSK(null);
	token.setPathSecurityParameters(psp);
	verifier.verifyPathSecurity();
    }

}
