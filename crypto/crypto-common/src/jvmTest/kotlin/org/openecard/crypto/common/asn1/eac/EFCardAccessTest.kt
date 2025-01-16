/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
package org.openecard.crypto.common.asn1.eac

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.ECardConstants.NPA_CARD_TYPE
import org.openecard.crypto.common.asn1.eac.ef.EFCardAccess
import org.openecard.crypto.common.asn1.eac.oid.CAObjectIdentifier
import org.openecard.crypto.common.asn1.eac.oid.EACObjectIdentifier
import org.openecard.crypto.common.asn1.eac.oid.PACEObjectIdentifier
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream

private val LOG = KotlinLogging.logger {  }

/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class EFCardAccessTest {
    private lateinit var efcaA: EFCardAccess
    private lateinit var efcaB: EFCardAccess

    @BeforeTest
    fun init() {
        // Standardized Domain Parameters
        var data = loadTestFile("EF_CardAccess.bin")
        var sis = SecurityInfos.getInstance(data)
        efcaA = EFCardAccess.getInstance(sis)

        // Proprietary Domain Parameters
        data = loadTestFile("EF_CardAccess_pdp.bin")
        sis = SecurityInfos.getInstance(data)
        efcaB = EFCardAccess.getInstance(sis)
    }

    @Throws(Exception::class)
    private fun loadTestFile(file: String): ByteArray {
        val path = "/$file"
        val ins = EFCardAccessTest::class.java.getResourceAsStream(path)
        val baos = ByteArrayOutputStream(ins!!.available())
        try {
            var b: Int
            while ((ins.read().also { b = it }) != -1) {
                baos.write(b.toByte().toInt())
            }
        } catch (e: Exception) {
			LOG.error(e) { "${e.message}"}
        }
        return baos.toByteArray()
    }

    @Test
    @Throws(Exception::class)
    fun testPACESecurityInfos() {
        var psi = efcaA.pACESecurityInfos
        val pip = psi.pACEInfoPairs[0]
        var pi = pip.pACEInfo
        val pdp = pip.createPACEDomainParameter()

        Assert.assertEquals(pi.protocol, "0.4.0.127.0.7.2.2.4.2.2")
        Assert.assertEquals(pi.protocol, PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128)
        Assert.assertEquals(pi.version, 2)
        Assert.assertEquals(pi.parameterID, 13)

        psi = efcaB.pACESecurityInfos
        pi = psi.pACEInfos[0]
        val pdpi = psi.pACEDomainParameterInfos[0]

        //	pdp = new PACEDomainParameter(psi);
        Assert.assertEquals(pi.protocol, "0.4.0.127.0.7.2.2.4.2.2")
        Assert.assertEquals(pi.protocol, PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128)
        Assert.assertEquals(pi.version, 1)
        Assert.assertEquals(pi.parameterID, -1)

        Assert.assertEquals(pdpi.protocol, "0.4.0.127.0.7.2.2.4.2")
        Assert.assertEquals(pdpi.protocol, PACEObjectIdentifier.id_PACE_ECDH_GM)
        Assert.assertEquals(pdpi.parameterID, 0)
        Assert.assertEquals(pdpi.domainParameter.objectIdentifier, "0.4.0.127.0.7.1.1.5.2.2.2")
    }

    @Test
    @Throws(Exception::class)
    fun testCASecurityInfos() {
        var csi = efcaA.cASecurityInfos
        val cdp = CADomainParameter(csi)

        var ci = csi.cAInfos[0]
        Assert.assertEquals(ci.protocol, "0.4.0.127.0.7.2.2.3.2.2")
        Assert.assertEquals(ci.protocol, CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128)
        Assert.assertEquals(ci.version, 2)
        Assert.assertEquals(ci.keyID, 65)

        ci = csi.cAInfos[1]
        Assert.assertEquals(ci.protocol, "0.4.0.127.0.7.2.2.3.2.2")
        Assert.assertEquals(ci.protocol, CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128)
        Assert.assertEquals(ci.version, 2)
        Assert.assertEquals(ci.keyID, 69)

        var cdpi = csi.cADomainParameterInfos[0]
        Assert.assertEquals(cdpi.protocol, "0.4.0.127.0.7.2.2.3.2")
        Assert.assertEquals(cdpi.protocol, CAObjectIdentifier.id_CA_ECDH)
        Assert.assertEquals(cdpi.domainParameter.objectIdentifier, "0.4.0.127.0.7.1.2")
        Assert.assertEquals(cdpi.domainParameter.objectIdentifier, EACObjectIdentifier.standardized_Domain_Parameters)
        Assert.assertEquals(cdpi.domainParameter.parameters.toString(), "13")
        Assert.assertEquals(cdpi.keyID, 65)

        cdpi = csi.cADomainParameterInfos[1]
        Assert.assertEquals(cdpi.protocol, "0.4.0.127.0.7.2.2.3.2")
        Assert.assertEquals(cdpi.protocol, CAObjectIdentifier.id_CA_ECDH)
        Assert.assertEquals(cdpi.domainParameter.objectIdentifier, "0.4.0.127.0.7.1.2")
        Assert.assertEquals(cdpi.domainParameter.objectIdentifier, EACObjectIdentifier.standardized_Domain_Parameters)
        Assert.assertEquals(cdpi.domainParameter.parameters.toString(), "13")
        Assert.assertEquals(cdpi.keyID, 69)

        csi = efcaB.cASecurityInfos
        ci = csi.cAInfos[0]
        cdpi = csi.cADomainParameterInfos[0]

        //	cdp = new CADomainParameter(csi);
        Assert.assertEquals(ci.protocol, "0.4.0.127.0.7.2.2.3.2.2")
        Assert.assertEquals(ci.protocol, CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128)
        Assert.assertEquals(ci.version, 2)
        Assert.assertEquals(ci.keyID, 0)

        Assert.assertEquals(cdpi.protocol, "0.4.0.127.0.7.2.2.3.2")
        Assert.assertEquals(cdpi.protocol, CAObjectIdentifier.id_CA_ECDH)
        Assert.assertEquals(cdpi.domainParameter.objectIdentifier, "0.4.0.127.0.7.1.1.5.2.2.2")
    }

    @Test
    @Throws(Exception::class)
    fun testTASecurityInfos() {
        var tsi = efcaA.tASecurityInfos
        var ti = tsi.tAInfos[0]

        Assert.assertEquals(ti.protocol, "0.4.0.127.0.7.2.2.2")
        Assert.assertEquals(ti.protocol, EACObjectIdentifier.id_TA)
        Assert.assertEquals(ti.version, 2)

        tsi = efcaB.tASecurityInfos
        ti = tsi.tAInfos[0]

        Assert.assertEquals(ti.protocol, "0.4.0.127.0.7.2.2.2")
        Assert.assertEquals(ti.protocol, EACObjectIdentifier.id_TA)
        Assert.assertEquals(ti.version, 2)
    }

    @Test
    @Throws(Exception::class)
    fun testCardInfoLocator() {
        var cil = efcaA.cardInfoLocator

        Assert.assertEquals(cil!!.protocol, "0.4.0.127.0.7.2.2.6")
        Assert.assertEquals(cil.protocol, EACObjectIdentifier.id_CI)
        Assert.assertEquals(cil.uRL, NPA_CARD_TYPE)
        Assert.assertNull(cil.eFCardInfo)

        cil = efcaB.cardInfoLocator

        Assert.assertEquals(cil!!.protocol, "0.4.0.127.0.7.2.2.6")
        Assert.assertEquals(cil.protocol, EACObjectIdentifier.id_CI)
        Assert.assertEquals(cil.uRL, "AwT ePA - BDr GmbH - Testkarte v1.0")
        Assert.assertNull(cil.eFCardInfo)
    }

}
