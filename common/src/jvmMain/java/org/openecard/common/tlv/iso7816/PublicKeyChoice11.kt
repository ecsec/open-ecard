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
class PublicKeyChoice(tlv: TLV?) : TLVType(tlv) {
    /**
     * Gets the corresponding TLV object to the element name.
     *
     * @return A TLV object containing a PublicKeyObject from ISO7816-15
     */
    var elementValue: GenericPublicKeyObject<TLV?>? = null
        private set
    private var extension: TLV? = null

    /**
     * The method returns the name of the key type which is contained in the PublicKeyChoice object.
     *
     * @return One of the following strings will be returned: <br></br>
     * - publicRSAKey<br></br>
     * - publicECKey<br></br>
     * - publicDHKey<br></br>
     * - publicDSAKey<br></br>
     * - publicKEAKey<br></br>
     * - genericPublicKey<br></br>
     * - extension
     */
    var elementName: String? = null
        private set

    init {
        val p = Parser(tlv)

        if (p.match(Tag.Companion.SEQUENCE_TAG)) {
            elementName = "publicRSAKey"
            elementValue = GenericPublicKeyObject<Any?>(p.next(0), TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
            elementName = "publicECKey"
            elementValue = GenericPublicKeyObject<Any?>(p.next(0), TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
            elementName = "publicDHKey"
            elementValue = GenericPublicKeyObject<Any?>(p.next(0), TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 2))) {
            elementName = "publicDSAKey"
            elementValue = GenericPublicKeyObject<Any?>(p.next(0), TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 3))) {
            elementName = "publicKEAKey"
            elementValue = GenericPublicKeyObject<Any?>(p.next(0), TLV::class.java)
        } else if (p.match(Tag(TagClass.CONTEXT, false, 4))) {
            elementName = "genericPublicKey"
            elementValue = GenericPublicKeyObject<Any?>(p.next(0), TLV::class.java)
        } else {
            elementName = "extension"
            extension = p.next(0)
        }
    }
}
