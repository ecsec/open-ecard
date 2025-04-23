/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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
 ***************************************************************************/
package org.openecard.crypto.common.asn1.eac

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.bouncycastle.asn1.ASN1Set
import org.openecard.bouncycastle.asn1.ASN1StreamParser
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class SecurityInfos
/**
	 * Instantiates a new set of SecurityInfos.
	 *
	 * @param securityInfos the ASN1 encoded SecurityInfos set
	 */
	private constructor(
		/**
		 * Gets the SecurityInfos.
		 *
		 * @return the SecurityInfos
		 */
		val securityInfos: ASN1Set,
	) {
		companion object {
			/**
			 * Gets the single instance of SecurityInfos.
			 *
			 * @param obj
			 * @return single instance of SecurityInfos
			 */
			@JvmStatic
			fun getInstance(obj: Any): SecurityInfos {
				if (obj is SecurityInfos) {
					return obj
				} else if (obj is ASN1Set) {
					return SecurityInfos(obj)
				} else if (obj is ByteArray) {
					return getInstance(ByteArrayInputStream(obj))
				} else if (obj is InputStream) {
					try {
						val sp = ASN1StreamParser(obj)
						val enc = sp.readObject()
						return getInstance(enc.toASN1Primitive())
					} catch (e: IOException) {
						LOG.error(e) { "Cannot parse SecurityInfos" }
					}
				}
				throw IllegalArgumentException("Unknown object in factory: " + obj.javaClass)
			}
		}
	}
