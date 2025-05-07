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

import org.openecard.common.tlv.Parser
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.Tag
import org.openecard.common.tlv.TagClass
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

/**
 *
 * @author Tobias Wich
 */
class GenericObjectValue<Type>(
	tlv: TLV?,
	clazz: Class<Type>,
) : TLVType(tlv) {
	private var indirect: ReferencedValue? = null
	private var direct: Type? = null

	init {
		val c: Constructor<Type>
		try {
			c = clazz.getConstructor(TLV::class.java)
		} catch (ex: Exception) {
			throw TLVException("Type supplied doesn't have a constructor Type(TLV).")
		}

		val p = Parser(tlv)

		if (p.match(Tag.Companion.SEQUENCE_TAG) ||
			p.match(Tag(TagClass.UNIVERSAL, true, 19)) ||
			p.match(Tag(TagClass.UNIVERSAL, true, 22)) ||
			p.match(Tag(TagClass.CONTEXT, false, 3))
		) {
			indirect = ReferencedValue(p.next(0))
		} else if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
			try {
				direct = c.newInstance(p.next(0)!!.child)
			} catch (ex: InvocationTargetException) {
				throw TLVException(ex)
			} catch (ex: Exception) {
				throw TLVException("Type supplied doesn't have a constructor Type(TLV).")
			}
		} else {
			throw TLVException("Unexpected element in ObjectValue.")
		}
	}
}
