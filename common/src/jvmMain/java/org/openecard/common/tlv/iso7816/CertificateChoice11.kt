/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import org.openecard.common.tlv.Parser
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.Tag
import org.openecard.common.tlv.TagClass

/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class CertificateChoice(tlv: TLV?) : TLVType(tlv) {
    var x509Certificate: GenericCertificateObject<X509CertificateAttribute>? = null // X509CertificateAttributes
        private set
    var x509AttributeCertificate: GenericCertificateObject<TLV>? = null // X509AttributeCertificateAttributes
        private set
    var spkiCertificate: GenericCertificateObject<TLV>? = null // SPKICertificateAttributes
        private set
    var pgpCertificate: GenericCertificateObject<TLV>? = null // PGPCertificateAttributes
        private set
    var wtlsCertificate: GenericCertificateObject<TLV>? = null // WTLSCertificateAttributes
        private set
    var x9_68Certificate: GenericCertificateObject<TLV>? = null // X9-68CertificateAttributes
        private set
    var cvCertificate: GenericCertificateObject<TLV>? = null // CVCertificateAttributes
        private set
    var genericCertificateObject: GenericCertificateObject<TLV>? = null // GenericCertificateAttributes
        private set
    var extension: TLV? = null
        private set


    init {
        val p = Parser(tlv)

        if (p.match(Tag(TagClass.UNIVERSAL, false, 16))) {
            x509Certificate = GenericCertificateObject(p.next(0)!!, X509CertificateAttribute::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
            x509AttributeCertificate = GenericCertificateObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
            spkiCertificate = GenericCertificateObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 2))) {
            pgpCertificate = GenericCertificateObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 3))) {
            wtlsCertificate = GenericCertificateObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 4))) {
            x9_68Certificate = GenericCertificateObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 5))) {
            cvCertificate = GenericCertificateObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 6))) {
            genericCertificateObject = GenericCertificateObject(p.next(0)!!, TLV::class.java)
        } else {
            extension = p.next(0)
        }
    }
}
