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
package org.openecard.crypto.common.asn1.cvc

import org.openecard.common.util.StringUtils
import org.testng.Assert
import java.util.*
import kotlin.test.Test

/**
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
class CardVerifiableCertificateTest {
    @Test
    fun test() {
        val c =
            StringUtils.toByteArray("7F218201487F4E8201005F290100420E5A5A4456434141544230303030377F494F060A04007F0007020202020386410453107B1FA3767A3A36532A7CA1AE2BF2B3D08B6508CE03FECD9397CB107318519442980E9F17239A976FB1800A5515BC4AF61B013F7C5454A22A86D0CE18FADA5F20105A5A41546D74475465737430303030307F4C12060904007F0007030102025305300301FFB75F25060100010001035F2406010001000300655E732D060904007F00070301030280208FAC553CB79699D13E724E864BEBDD818DD550F7C34FC170ECDE2598A03F9EAC732D060904007F0007030103018020B48DA6DC54E8440F41EB20358CE8F640F45838D68B3E39812600047DBC5BB93B5F37405EE54A76BA698C098750E5E559F79CE2463E3F812083BB3815F4A7322C117C007C9D23958E99EC9542924BEF910A8C4C6462FB4D33B0F50F6B946F3A641C0DB1")
        val body = StringUtils.toByteArray("5F290100420E5A5A4456434141544230303030377F494F060A04007F0007020202020386410453107B1FA3767A3A36532A7CA1AE2BF2B3D08B6508CE03FECD9397CB107318519442980E9F17239A976FB1800A5515BC4AF61B013F7C5454A22A86D0CE18FADA5F20105A5A41546D74475465737430303030307F4C12060904007F0007030102025305300301FFB75F25060100010001035F2406010001000300655E732D060904007F00070301030280208FAC553CB79699D13E724E864BEBDD818DD550F7C34FC170ECDE2598A03F9EAC732D060904007F0007030103018020B48DA6DC54E8440F41EB20358CE8F640F45838D68B3E39812600047DBC5BB93B")
        val signature = StringUtils.toByteArray("5EE54A76BA698C098750E5E559F79CE2463E3F812083BB3815F4A7322C117C007C9D23958E99EC9542924BEF910A8C4C6462FB4D33B0F50F6B946F3A641C0DB1")
        val chat = StringUtils.toByteArray("7F4C12060904007F0007030102025305300301FFB7")
        val chr = StringUtils.toByteArray("5A5A41546D7447546573743030303030")
        val car = StringUtils.toByteArray("5A5A445643414154423030303037")
        val publicKey = StringUtils.toByteArray("060A04007F0007020202020386410453107B1FA3767A3A36532A7CA1AE2BF2B3D08B6508CE03FECD9397CB107318519442980E9F17239A976FB1800A5515BC4AF61B013F7C5454A22A86D0CE18FADA")
        val cpi = byteArrayOf(0x00)
        val encodedBodyAndSignature = StringUtils.toByteArray("7F4E8201005F290100420E5A5A4456434141544230303030377F494F060A04007F0007020202020386410453107B1FA3767A3A36532A7CA1AE2BF2B3D08B6508CE03FECD9397CB107318519442980E9F17239A976FB1800A5515BC4AF61B013F7C5454A22A86D0CE18FADA5F20105A5A41546D74475465737430303030307F4C12060904007F0007030102025305300301FFB75F25060100010001035F2406010001000300655E732D060904007F00070301030280208FAC553CB79699D13E724E864BEBDD818DD550F7C34FC170ECDE2598A03F9EAC732D060904007F0007030103018020B48DA6DC54E8440F41EB20358CE8F640F45838D68B3E39812600047DBC5BB93B5F37405EE54A76BA698C098750E5E559F79CE2463E3F812083BB3815F4A7322C117C007C9D23958E99EC9542924BEF910A8C4C6462FB4D33B0F50F6B946F3A641C0DB1")
        val extension = StringUtils.toByteArray("732D060904007F00070301030280208FAC553CB79699D13E724E864BEBDD818DD550F7C34FC170ECDE2598A03F9EAC732D060904007F0007030103018020B48DA6DC54E8440F41EB20358CE8F640F45838D68B3E39812600047DBC5BB93B")

        var cvc = CardVerifiableCertificate(c)

        Assert.assertEquals(body, cvc.body)
        Assert.assertEquals(signature, cvc.signature)
        Assert.assertEquals(chat, cvc.cHAT.toByteArray())
        Assert.assertEquals(chr, cvc.cHR.toByteArray())
        Assert.assertEquals(car, cvc.cAR.toByteArray())
        Assert.assertEquals(publicKey, cvc.getPublicKey().tLVEncoded.getValue())
        Assert.assertEquals(cpi, cvc.cPI)
        Assert.assertEquals(encodedBodyAndSignature, cvc.certificate.getValue())
        Assert.assertEquals(extension, cvc.extensions)

        Assert.assertEquals(13, cvc.getEffectiveDate().get(GregorianCalendar.DAY_OF_MONTH))
        Assert.assertEquals(9, cvc.getEffectiveDate().get(GregorianCalendar.MONTH))
        Assert.assertEquals(2010, cvc.getEffectiveDate().get(GregorianCalendar.YEAR))

        Assert.assertEquals(30, cvc.getExpirationDate().get(GregorianCalendar.DAY_OF_MONTH))
        Assert.assertEquals(9, cvc.getExpirationDate().get(GregorianCalendar.MONTH))
        Assert.assertEquals(2010, cvc.getExpirationDate().get(GregorianCalendar.YEAR))

        try {
            cvc = CardVerifiableCertificate(encodedBodyAndSignature)
        } catch (expected: Exception) {
        }
    }
}
