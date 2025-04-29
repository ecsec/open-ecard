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
class PrivateKeyChoice(tlv: TLV?) : TLVType(tlv) {
    var privateRSAKey: GenericPrivateKeyObject<PrivateRSAKeyAttributes>? = null
        private set
    var privateECKey: GenericPrivateKeyObject<PrivateECKeyAttributes>? = null
        private set
    var privateDHKey: GenericPrivateKeyObject<TLV>? = null
        private set
    var privateDSAKey: GenericPrivateKeyObject<TLV>? = null
        private set
    var privateKEAKey: GenericPrivateKeyObject<TLV>? = null
        private set
    var genericPrivateKey: GenericPrivateKeyObject<TLV>? = null
        private set
    var ext: TLV? = null
        private set

    init {
        val p = Parser(tlv)

        if (p.match(Tag(TagClass.UNIVERSAL, false, 16))) {
            privateRSAKey = GenericPrivateKeyObject(p.next(0)!!, PrivateRSAKeyAttributes::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
            privateECKey = GenericPrivateKeyObject(p.next(0)!!, PrivateECKeyAttributes::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
            privateDHKey = GenericPrivateKeyObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 2))) {
            privateDSAKey = GenericPrivateKeyObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 3))) {
            privateKEAKey = GenericPrivateKeyObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 4))) {
            genericPrivateKey = GenericPrivateKeyObject(p.next(0)!!, TLV::class.java)
        } else {
            ext = p.next(0)
        }
    }
}
