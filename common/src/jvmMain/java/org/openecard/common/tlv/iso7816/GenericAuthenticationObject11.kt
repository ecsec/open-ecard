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

import org.openecard.common.tlv.*
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

/**
 * The class implements a data type which corresponds to the ASN.1 type AuthenticationObject in ISO7816-15.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 * @param <AuthAttributes>
</AuthAttributes> */
class GenericAuthenticationObject<AuthAttributes>(tlv: TLV, clazz: Class<AuthAttributes>) {
    /**
     * Gets the object itself as TLV.
     *
     * @return The object as TLV.
     */
    /**
     * The TLV which represents this object
     */
    val genericAuthenticationObjectTLV: TLV

    /**
     * Gets the [CommonObjectAttributes] of the object.
     *
     * @return The CommonObjectAttribute of the object.
     */
    // from CIO
    var commonObjectAttributes: CommonObjectAttributes? = null
        private set

    /**
     * Gets the class attributes of this object.
     *
     * @return The class attributes of the object as TLV.
     */
    var classAttributes: TLV? = null // CommonAuthObjectAttributes
        private set

    /**
     * Gets the sub class attributes of the object.
     *
     * @return The sub class attributes as TLV.
     */
    var subClassAttributes: TLV? = null // NULL
        private set

    /**
     * Gets the generic object of this datatype.
     * The returned data type depends on the specification in the constructor.
     *
     * @return The authentication attributes of the object.
     */
    var authAttributes: AuthAttributes? = null // AuthObjectAttributes
        private set

    /**
     * The constructor parses the input TLV and instantiates the generic part of the class.
     *
     * @param tlv The [TLV] which will be used to create the object.
     * @param clazz Class type of the generic attribute.
     * @throws TLVException
     */
    init {
        val c: Constructor<AuthAttributes>
        try {
            c = clazz.getConstructor(TLV::class.java)
        } catch (ex: Exception) {
            throw TLVException("AuthAttributes supplied doesn't have a constructor AuthAttributes(TLV).")
        }

        this.genericAuthenticationObjectTLV = tlv

        // parse the tlv
        val p = Parser(tlv.child)
        if (p.match(Tag.Companion.SEQUENCE_TAG)) {
            commonObjectAttributes = CommonObjectAttributes(p.next(0)!!)
        } else {
            throw TLVException("CommonObjectAttributes not present.")
        }
        if (p.match(Tag.Companion.SEQUENCE_TAG)) {
            classAttributes = p.next(0)
        } else {
            throw TLVException("CommonObjectAttributes not present.")
        }
        if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
            subClassAttributes = p.next(0)!!.child
        }
        if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
            try {
                authAttributes = c.newInstance(p.next(0)!!.child)
            } catch (ex: InvocationTargetException) {
                throw TLVException(ex)
            } catch (ex: Exception) {
                throw TLVException("AuthAttributes supplied doesn't have a constructor AuthAttributes(TLV).")
            }
        }
    }
}
