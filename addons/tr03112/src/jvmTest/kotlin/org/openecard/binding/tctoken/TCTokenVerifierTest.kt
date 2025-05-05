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
 */
package org.openecard.binding.tctoken

import generated.TCTokenType
import org.openecard.binding.tctoken.ex.ActivationError
import org.openecard.binding.tctoken.ex.InvalidTCTokenElement
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.httpcore.ResourceContext
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.util.*

/**
 * @author Moritz Horsch
 */
class TCTokenVerifierTest {
    private var token: TCToken? = null
    private var verifier: TCTokenVerifier? = null

    @BeforeTest
    @Throws(Exception::class)
    fun initTestObject() {
        val testFile = resolveResourceAsStream(javaClass, "TCToken.xml")

        val parser = TCTokenParser()
        val tokens: MutableList<TCToken> = parser.parse(testFile!!)
        token = tokens.get(0)
        verifier = TCTokenVerifier(token!!, ResourceContext(null, null, Collections.EMPTY_LIST))
    }

    @Test
    @Throws(Exception::class)
    fun testVerify() {
        verifier!!.verifyUrlToken()
    }

    @Test(expectedExceptions = [InvalidTCTokenElement::class])
    @Throws(ActivationError::class)
    fun testVerifyServerAddress() {
        token!!.setServerAddress(null)
        verifier!!.verifyServerAddress()
    }

    @Test(expectedExceptions = [InvalidTCTokenElement::class])
    @Throws(ActivationError::class)
    fun testVerifySessionIdentifier() {
        token!!.setSessionIdentifier("")
        verifier!!.verifySessionIdentifier()
    }

    @Test(expectedExceptions = [InvalidTCTokenElement::class])
    @Throws(ActivationError::class)
    fun testVerifyRefreshAddress() {
        token!!.setRefreshAddress(null)
        token!!.setCommunicationErrorAddress("https://localhost/error")
        verifier!!.verifyRefreshAddress()
    }

    @Test(expectedExceptions = [InvalidTCTokenElement::class])
    @Throws(ActivationError::class)
    fun testVerifyBinding() {
        token!!.setBinding("urn:liberty:city:2006-08")
        verifier!!.verifyBinding()
    }

    @Test(expectedExceptions = [InvalidTCTokenElement::class], enabled = false)
    @Throws(ActivationError::class)
    fun testVerifyPathSecurityProtocol() {
        token!!.setPathSecurityProtocol(PATH_SEC_PROTO_TLS_PSK + "1")
        verifier!!.verifyPathSecurity()
    }

    @Test(expectedExceptions = [InvalidTCTokenElement::class], enabled = false)
    @Throws(ActivationError::class)
    fun testVerifyPathSecurityParameters() {
        token!!.setPathSecurityProtocol(PATH_SEC_PROTO_TLS_PSK)
        token!!.setPathSecurityParameters(null)
        verifier!!.verifyPathSecurity()
    }

    @Test(expectedExceptions = [InvalidTCTokenElement::class], enabled = false)
    @Throws(ActivationError::class)
    fun testVerifyPathSecurityParameters2() {
        val psp = TCTokenType.PathSecurityParameters()
        psp.setPSK(null)
        token!!.setPathSecurityParameters(psp)
        verifier!!.verifyPathSecurity()
    }
}
