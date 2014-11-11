/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.binding.tctoken.ex;

import org.openecard.common.I18nKey;


/**
 * Enum containing error translation keys for the TR-03112 addon.
 *
 * @author Tobias Wich
 */
public enum ErrorTranslations implements I18nKey {

    // TODO: sort the list
    NO_MESSAGE_ID("paos.exception.no_message_id"),
    MESSAGE_ID_MISSMATCH("paos.exception.message_id_mismatch"),
    CONNECTION_CLOSED("paos.exception.conncetion_closed"),
    INVALID_HTTP_STATUS("paos.exception.invalid_http_status_code"),
    DELIVERY_FAILED("paos.exception.failed_delivery"),
    SOAP_MESSAGE_FAILURE("paos.exception.soap_message_creation_failed"),
    MARSHALLING_ERROR("dispatcher.exception.failed_jaxb_object_marshaling"),
    DISPATCHER_ERROR("dispatcher.exception.dispatched_method_exception"),
    RETRIEVAL_FAILED("tctoken.retrieval.exception"),
    NO_TCTOKEN_IN_DATA("invalid.tctoken.exception.no_tctoken"),
    ESERVICE_ERROR("auth.server.exception"),
    INVALID_ADDRESS("invalid.address.exception.no_https"),
    MAX_REDIRECTS("resource.exception.max_redirects_exceeded"),
    MISSING_LOCATION_HEADER("resource.exception.no_location_header"),
    INVALID_RESULT_STATUS("invalid.result.status.exception"),
    FAILED_PROXY("io.exception.failed_proxy_initialization"),
    NO_URL("invalid.redirect.url.exception.no_url"),
    NO_REDIRECT_AVAILABLE("invalid.redirect.url.exception.no_url_available"),
    REFRESH_DETERMINATION_FAILED("invalid.redirect.url.exception.refresh_address_determination_failed"),
    REFRESH_URL_ERROR("illegal.state.exception.invalid_refresh_address_in_tctoken"),
    NO_RESPONSE_FROM_SERVER("paos.exception.no_response_from_server"),
    UNKNOWN_ECARD_ERROR("paos.exception.unknown_ecard_exception"),
    NO_PARAMS("missing.activation.parameter.exception.no_suitable_parameters"),
    NO_TOKEN("missing.activation.parameter.exception.no_valid_tctoken_available"),
    INVALID_TCTOKEN_URL("invalid.tctoken.url.exception.invalid_tctoken_url"),
    INVALID_ELEMENT("invalid.tctoken.element.invalid_element"),
    MISSING_ELEMENT("invalid.tctoken.element.missing_element"),
    MALFORMED_URL("invalid.tctoken.url.exception.malformed_url"),
    NO_HTTPS_URL("invalid.tctoken.url.exception.no_https_url"),
    FAILED_SOP("security.violation.exception.no_sop_tls2"),
    ESERVICE_FAIL("invalid.tctoken.element.eservice"),
    INVALID_REFRESH_ADDRESS("invalid.tctoken.element.invalid_refresh_address"),
    NO_REFRESH_ADDRESS("invalid.tctoken.element.no_refresh_address"),
    NO_SERVER_ADDRESS("invalid.tctoken.element.no_server_address"),
    ERROR_TITLE("error"),
    FINISH_TITLE("finish"),
    REMOVE_CARD("remove_card_msg"),
    ERROR_HEADER("err_header"),
    ERROR_MSG_IND("err_msg_indicator"),
    ACTIVATION_INVALID_REFRESH_ADDRESS("activation.action.invalid_refresh_address"),
    WRONG_SERVER_RESULT("connection.error.invalid_status_code"),
    REDIRECT_MALFORMED_URL("redirect.cert.validator.malformed_subject_url"),
    INVALID_REDIRECT("redirect.cert.validator.invalid_redirect"),
    MALFORMED_TOKEN("invalid.tctoken.exception.malformed_tctoken"),
    UNSUPPORTED_FEATURE("illegal.argument.exception.unsupported_parser_feature"),
    INVALID_URL("illegal.argument.exception.invalid_url"),
    UNKNOWN_SEC_PROTOCOL("connection.error.unknown_sec_protocol"),
    INVALID_CERT("element.parsing.exception.invalid_cert_number"),
    INVALID_SIGNATURE_NUMBER("element.parsing.exception.invalid_signature_number"),
    PAOS_CONNECTION_EXCEPTION("paos.connection.exception");

    private final String key;

    private ErrorTranslations(String key) {
	this.key = key;
    }

    @Override
    public String getKey() {
	return key;
    }

}
