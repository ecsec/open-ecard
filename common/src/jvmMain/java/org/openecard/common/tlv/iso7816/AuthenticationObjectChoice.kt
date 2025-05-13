/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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

/**
 * The class implements a data type which represents the ASN.1 notation of the AuthenticationObjectChoice in ISO7816-15.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class AuthenticationObjectChoice(
	tlv: TLV,
) : TLVType(tlv) {
	var passwordObject: GenericAuthenticationObject<PasswordAttributes>? = null // PasswordAttributes
		private set
	var biometricTemplate: GenericAuthenticationObject<TLV>? = null // BiometricAttributes
		private set
	var authenticationKey: GenericAuthenticationObject<TLV>? = null // AuthKeyAttributes
		private set
	var externalAuthObject: GenericAuthenticationObject<TLV>? = null // ExternalAuthObjectAttributes
		private set
	var futureExtension: TLV? = null
		private set

	/**
	 * The constructor parses the input [TLV] to set the element which is chosen.
	 *
	 * @param tlv The [TLV] which represents the AuthenticationObjectChoice.
	 * @throws TLVException
	 */
	init {
		val p = Parser(tlv)

		if (p.match(Tag(TagClass.UNIVERSAL, false, 16))) {
			passwordObject = GenericAuthenticationObject(p.next(0)!!, PasswordAttributes::class.java)
		} else if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
			biometricTemplate = GenericAuthenticationObject(p.next(0)!!, TLV::class.java)
		} else if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
			authenticationKey = GenericAuthenticationObject(p.next(0)!!, TLV::class.java)
		} else if (p.match(Tag(TagClass.CONTEXT, false, 2))) {
			externalAuthObject = GenericAuthenticationObject(p.next(0)!!, TLV::class.java)
		} else {
			futureExtension = p.next(0)
		}
	}
}
