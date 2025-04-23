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
 ***************************************************************************/
package org.openecard.ws.soap

import javax.xml.namespace.QName

/**
 *
 * @author Tobias Wich
 */
object SOAPConstants {
	const val SOAP_1_1_PROTOCOL: String = "SOAP 1.1 Protocol"
	const val SOAP_1_2_PROTOCOL: String = "SOAP 1.2 Protocol"
	const val DEFAULT_SOAP_PROTOCOL: String = SOAP_1_1_PROTOCOL

	const val URI_NS_SOAP_1_1_ENVELOPE: String = "http://schemas.xmlsoap.org/soap/envelope/"
	const val URI_NS_SOAP_1_2_ENVELOPE: String = "http://www.w3.org/2003/05/soap-envelope"
	const val URI_NS_SOAP_ENVELOPE: String = URI_NS_SOAP_1_1_ENVELOPE

	const val URI_NS_SOAP_ENCODING: String = "http://schemas.xmlsoap.org/soap/encoding/"
	const val URI_NS_SOAP_1_2_ENCODING: String = "http://www.w3.org/2003/05/soap-encoding"

	const val SOAP_1_1_CONTENT_TYPE: String = "text/xml"
	const val SOAP_1_2_CONTENT_TYPE: String = "application/soap+xml"

	const val URI_SOAP_ACTOR_NEXT: String = "http://schemas.xmlsoap.org/soap/actor/next"
	const val URI_SOAP_1_2_ROLE_NEXT: String = URI_NS_SOAP_1_2_ENVELOPE + "/role/next"
	const val URI_SOAP_1_2_ROLE_NONE: String = URI_NS_SOAP_1_2_ENVELOPE + "/role/none"
	const val URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER: String = URI_NS_SOAP_1_2_ENVELOPE + "/role/ultimateReceiver"

	const val SOAP_ENV_PREFIX: String = "env"

	val SOAP_VERSIONMISMATCH_FAULT: QName = QName(URI_NS_SOAP_1_2_ENVELOPE, "VersionMismatch", SOAP_ENV_PREFIX)
	val SOAP_MUSTUNDERSTAND_FAULT: QName = QName(URI_NS_SOAP_1_2_ENVELOPE, "MustUnderstand", SOAP_ENV_PREFIX)
	val SOAP_DATAENCODINGUNKNOWN_FAULT: QName = QName(URI_NS_SOAP_1_2_ENVELOPE, "DataEncodingUnknown", SOAP_ENV_PREFIX)
	val SOAP_SENDER_FAULT: QName = QName(URI_NS_SOAP_1_2_ENVELOPE, "Sender", SOAP_ENV_PREFIX)
	val SOAP_RECEIVER_FAULT: QName = QName(URI_NS_SOAP_1_2_ENVELOPE, "Receiver", SOAP_ENV_PREFIX)
}
