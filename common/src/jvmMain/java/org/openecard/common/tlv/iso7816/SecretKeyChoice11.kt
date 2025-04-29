/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
 * @author Hans-Martin Haase
 */
class SecretKeyChoice(tlv: TLV) : TLVType(tlv) {
    var algIndependentKey: GenericSecretKeyObject<TLV>? = null
        private set
    var genericSecretKey: GenericSecretKeyObject<TLV>? = null
        private set
    var extension: TLV? = null
        private set

    init {
        val p = Parser(tlv.child)
        if (p.match(Tag.Companion.SEQUENCE_TAG)) {
            algIndependentKey = GenericSecretKeyObject(p.next(0)!!, TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 15))) {
            genericSecretKey = GenericSecretKeyObject(p.next(0)!!, TLV::class.java)
        } else {
            extension = p.next(0)
        }
    }
}
