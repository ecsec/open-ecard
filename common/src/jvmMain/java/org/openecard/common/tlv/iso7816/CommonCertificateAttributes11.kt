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
class CommonCertificateAttributes(tlv: TLV?) : TLVType(tlv) {
    /**
     * Get the value of the id property.
     * The id corresponds to a id of a private or public key.
     *
     * @return The id of the certificate which corresponds to a private or public key.
     */
    /**
     * General identifier used to identify the corresponding private/public key.
     */
    var id: ByteArray?
        private set

    var isAuthority: Boolean = false
        private set

    /**
     * Get the value of the identifier property.
     * The identifier here is a credential identifier.
     *
     * @return Identifier structure.
     */
    /**
     * Credential identifier.
     */
    var identifier: TLV? = null
        private set

    var certHash: TLV? = null
        private set

    var trustedUsage: TLV? = null
        private set

    /**
     * List of credential identifiers.
     *
     * @return Identifiers structure.
     */
    var identifiers: TLV? = null
        private set

    var validity: TLV? = null
        private set

    init {
        val p = Parser(tlv)

        if (p.match(Tag(TagClass.UNIVERSAL, true, 2))) {
            id = p.next(0).value
        } else {
            throw TLVException("Missing ID field in the CommonCertificateAttributes")
        }

        if (p.match(Tag(TagClass.UNIVERSAL, true, 1))) {
            if (toInteger(p.next(0).value) == 0) {
                isAuthority = false
            } else {
                isAuthority = true
            }
        }

        if (p.match(Tag.Companion.SEQUENCE_TAG)) {
            identifier = p.next(0)
        }

        if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
            certHash = p.next(0)
        }

        if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
            trustedUsage = p.next(0)
        }

        if (p.match(Tag(TagClass.CONTEXT, false, 2))) {
            identifiers = p.next(0)
        }

        /*
	 * NOTE: context tag 3 is reserved for historical reasons (PKCS#15)
	 */
        if (p.match(Tag(TagClass.CONTEXT, false, 4))) {
            validity = p.next(0)
        }
    }
}
