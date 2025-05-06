/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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
package org.openecard.binding.tctoken

/**
 * Class with keys to access values inside a DynamicContext.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
object TR03112Keys {
	const val INSTANCE_KEY: String = "tr03112"

	const val TCTOKEN_CHECKS: String = "tctoken_checks"
	const val ACTIVATION_THREAD: String = "activation_thread"
	const val SESSION_CON_HANDLE: String = "session_connection_handle"
	const val CONNECTION_HANDLE: String = "connection_handle"
	const val SAME_CHANNEL: String = "same_channel"
	const val ESERVICE_CERTIFICATE_DESC: String = "eservice_certificate_description"
	const val EIDSERVER_CERTIFICATE: String = "eservice_certificate"
	const val TCTOKEN_URL: String = "TCTokenURL"
	const val TCTOKEN_SERVER_CERTIFICATES: String = "tctoken_server_certificates"
	const val IS_REFRESH_URL_VALID: String = "is_refresh_url_valid"
	const val OPEN_USER_CONSENT_THREAD: String = "user_consent_thread"
	const val COOKIE_MANAGER: String = "cookie_mananger"
	const val CARD_SELECTION_CANCELLATION: String = "card_selection_canceld"
	const val ACTIVATION_CARD_TYPE: String = "activation_card_type"
}
