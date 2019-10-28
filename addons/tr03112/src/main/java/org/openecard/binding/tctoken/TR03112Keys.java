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
 ***************************************************************************/

package org.openecard.binding.tctoken;


/**
 * Class with keys to access values inside a DynamicContext.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class TR03112Keys {

    public static final String INSTANCE_KEY = "tr03112";

    public static final String TCTOKEN_CHECKS = "tctoken_checks";
    public static final String ACTIVATION_THREAD = "activation_thread";
    public static final String CONNECTION_HANDLE = "connection_handle";
    public static final String SAME_CHANNEL = "same_channel";
    public static final String ESERVICE_CERTIFICATE_DESC = "eservice_certificate_description";
    public static final String EIDSERVER_CERTIFICATE = "eservice_certificate";
    public static final String TCTOKEN_URL = "TCTokenURL";
    public static final String TCTOKEN_SERVER_CERTIFICATES = "tctoken_server_certificates";
    public static final String IS_REFRESH_URL_VALID = "is_refresh_url_valid";
    public static final String OPEN_USER_CONSENT_THREAD = "user_consent_thread";
    public static final String COOKIE_MANAGER = "cookie_mananger";
    public static final String CARD_SELECTION_CANCELLATION = "card_selection_canceld";
    public static final String ACTIVATION_CARD_TYPE = "activation_card_type";

}
