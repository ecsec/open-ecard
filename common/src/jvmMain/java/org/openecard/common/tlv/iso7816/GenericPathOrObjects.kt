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
import java.util.LinkedList

/**
 *
 * @author Tobias Wich
 */
class GenericPathOrObjects<KeyType : TLVType?>(
	tlv: TLV,
	clazz: Class<KeyType>,
) : TLV(tlv) {
	private var path: Path? = null
	private var objects: MutableList<KeyType>? = null
	private var ext: TLV? = null

	init {
		val c: Constructor<KeyType>
		try {
			c = clazz.getConstructor(TLV::class.java)
		} catch (ex: Exception) {
			throw TLVException("KeyType supplied doesn't have a constructor KeyType(TLV).")
		}

		val p = Parser(tlv.child)
		if (p.match(Tag.Companion.SEQUENCE_TAG)) {
			path = Path(p.next(0)!!)
		} else if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
			val p1 = Parser(p.next(0)!!.child)
			if (p1.match(Tag.Companion.SEQUENCE_TAG)) {
				val objectsList = TLVList(p1.next(0)!!)
				objects = LinkedList()
				for (nextT in objectsList.content) {
					try {
						(objects as LinkedList<KeyType>).add(c.newInstance(nextT))
					} catch (ex: InvocationTargetException) {
						throw TLVException(ex)
					} catch (ex: Exception) {
						throw TLVException("KeyType supplied doesn't have a constructor KeyType(TLV).")
					}
				}
			}
		} else if ((p.next(0).also { ext = it }) != null) {
			// fine already assigned
		} else {
			throw TLVException("No content in PathOrObject type.")
		}
	}

	fun hasPath(): Boolean = path != null

	fun path(): Path? = path

	fun hasObjects(): Boolean = objects != null

	fun objects(): List<KeyType>? = objects
}
