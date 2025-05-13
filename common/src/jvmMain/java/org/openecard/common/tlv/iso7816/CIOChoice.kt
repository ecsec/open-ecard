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

/**
 *
 * @author Tobias Wich
 */
class CIOChoice(
	tlv: TLV,
) : TLV(tlv) {
	var element: TLV? = null
		private set
	var elementName: String? = null
		private set

	private var privateKeys: GenericPathOrObjects<PrivateKeyChoice>? = null
	private var publicKeys: TLV? = null
	private var trustedPublicKeys: TLV? = null
	private var secretKeys: TLV? = null
	private var certificates: GenericPathOrObjects<CertificateChoice>? = null
	private var trustedCertificates: TLV? = null
	private var usefulCertificates: TLV? = null
	private var dataContainerObjects: TLV? = null
	private var authObjects: GenericPathOrObjects<AuthenticationObjectChoice>? = null
	private var futureExtension: TLV? = null

	init {
		val p = Parser(tlv)
		// process choice types
		if (p.match(Tag(TagClass.CONTEXT, false, 0))) {
			privateKeys = GenericPathOrObjects(p.next(0)!!, PrivateKeyChoice::class.java)
			element = privateKeys
			elementName = "privateKeys"
		} else if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
			publicKeys = p.next(0)
			element = publicKeys
			elementName = "publicKeys"
		} else if (p.match(Tag(TagClass.CONTEXT, false, 2))) {
			trustedPublicKeys = p.next(0)
			element = trustedPublicKeys
			elementName = "trustedPublicKeys"
		} else if (p.match(Tag(TagClass.CONTEXT, false, 3))) {
			secretKeys = p.next(0)
			element = secretKeys
			elementName = "secretKeys"
		} else if (p.match(Tag(TagClass.CONTEXT, false, 4))) {
			certificates = GenericPathOrObjects(p.next(0)!!, CertificateChoice::class.java)
			element = certificates
			elementName = "certificates"
		} else if (p.match(Tag(TagClass.CONTEXT, false, 5))) {
			trustedCertificates = p.next(0)
			element = trustedCertificates
			elementName = "trustedCertificates"
		} else if (p.match(Tag(TagClass.CONTEXT, false, 6))) {
			usefulCertificates = p.next(0)
			element = usefulCertificates
			elementName = "usefulCertificates"
		} else if (p.match(Tag(TagClass.CONTEXT, false, 7))) {
			dataContainerObjects = p.next(0)
			element = dataContainerObjects
			elementName = "dataContainerObjects"
		} else if (p.match(Tag(TagClass.CONTEXT, false, 8))) {
			authObjects = GenericPathOrObjects(p.next(0)!!, AuthenticationObjectChoice::class.java)
			element = authObjects
			elementName = "authObjects"
		} else { // extension
			futureExtension = p.next(0)
			element = futureExtension
			elementName = "futureExtension"
			if (futureExtension == null) {
				throw TLVException("No element in CIOChoice")
			}
		}
	}
}
