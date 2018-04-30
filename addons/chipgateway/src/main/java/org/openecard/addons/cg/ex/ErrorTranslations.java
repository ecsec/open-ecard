/****************************************************************************
 * Copyright (C) 2014-2016 ecsec GmbH.
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

package org.openecard.addons.cg.ex;

import org.openecard.common.I18nKey;


/**
 * Enum containing error translation keys for the ChipGateway addon.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public enum ErrorTranslations implements I18nKey {
    
    INVALID_REFRESH_ADDR("error.invalid_refresh_addr"),
    MALFORMED_URL("error.malformed_url"),
    NO_HTTPS_URL("error.no_https_url"),
    ELEMENT_MISSING("error.element_missing"),
    ELEMENT_VALUE_INVALID("error.element_value_invalid"),
    CONNECTION_OPEN_FAILED("error.connection_open_failed"),
    INVALID_HTTP_STATUS("error.invalid_http_status"),
    INVALID_CHIPGATEWAY_MSG("error.invalid_chipgateway_msg"),
    HTTP_ERROR("error.http_error"),
    SIGNATURE_INVALID("error.signature_invalid"),
    VERSION_OUTDATED("error.version_outdated"),
    UNKOWN("error.unknown"),
    SERVER_SENT_ERROR("error.server_sent_error");

    private final String key;

    private ErrorTranslations(String key) {
	this.key = key;
    }

    @Override
    public String getKey() {
	return key;
    }

}
