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

import org.openecard.common.tlv.Parser
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.Tag
import org.openecard.common.tlv.TagClass
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

/**
 *
 * @author Hans-Martin Haase
 */
class GenericDataContainerObject<ObjectType>
	@Throws(TLVException::class)
	constructor(
		tlv: TLV,
		clazz: Class<ObjectType>,
	) {
		/**
		 * The TLV which represents this object
		 */
		private val tlv: TLV

		// from CIO
		var commonObjectAttributes: CommonObjectAttributes? = null
			private set
		var classAttributes: TLV? = null // CommonDataContainerObjectAttributes
			private set
		var subClassAttributes: TLV? = null // NULL
			private set
		var typeAttributes: ObjectType? = null // DataObjectAttributes
			private set

		init {
			val c: Constructor<ObjectType>
			try {
				c = clazz.getConstructor(TLV::class.java)
			} catch (ex: Exception) {
				throw TLVException("AuthAttributes supplied doesn't have a constructor AuthAttributes(TLV).")
			}

			this.tlv = tlv

			// parse the tlv
			val p = Parser(tlv.child)
			if (p.match(Tag.SEQUENCE_TAG)) {
				commonObjectAttributes = CommonObjectAttributes(p.next(0)!!)
			} else {
				throw TLVException("CommonDataContainerObjectAttributes not present.")
			}
			if (p.match(Tag.SEQUENCE_TAG)) {
				classAttributes = p.next(0)
			} else {
				throw TLVException("CommonObjectAttributes not present.")
			}
			if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
				subClassAttributes = p.next(0)!!.child
			}
			if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
				try {
					typeAttributes = c.newInstance(p.next(0)!!.child)
				} catch (ex: InvocationTargetException) {
					throw TLVException(ex)
				} catch (ex: Exception) {
					throw TLVException("AuthAttributes supplied doesn't have a constructor AuthAttributes(TLV).")
				}
			}
		}
	}
