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
package org.openecard.common.tlv.iso7816

import org.openecard.common.tlv.*

/**
 *
 * @author Tobias Wich
 */
class X509CertificateAttribute(tlv: TLV) : TLVType(tlv) {
    private val value: GenericObjectValue<Certificate>
    private var subject: TLV? = null
    private var issuer: TLV? = null
    private var serialNumber: Int? = null


    init {
        val p = Parser(tlv.child)

        // first value is validated by GenericObjectValue
        value = GenericObjectValue(p.next(0), Certificate::class.java)

        if (p.match(Tag.Companion.SEQUENCE_TAG)) {
            subject = p.next(0)
        }
        if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
            issuer = p.next(0)
        }
        if (p.match(Tag.Companion.INTEGER_TAG)) {
            serialNumber = toInteger(p.next(0).value)
        }
    }
}
