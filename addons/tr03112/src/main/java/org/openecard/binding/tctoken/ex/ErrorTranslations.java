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

    ACTIVATION_INVALID_REFRESH_ADDRESS("activation.action.invalid_refresh_address"),
    CONNECTION_CLOSED("paos.exception.conncetion_closed"),
    DELIVERY_FAILED("paos.exception.failed_delivery"),
    DISPATCHER_ERROR("dispatcher.exception.dispatched_method_exception"),
    ERROR_HEADER("err_header"),
    ERROR_MSG_IND("err_msg_indicator"),
    ERROR_TITLE("error"),
    ESERVICE_ERROR("auth.server.exception"),
    ESERVICE_FAIL("invalid.tctoken.element.eservice"),
    FAILED_PROXY("io.exception.failed_proxy_initialization"),
    FAILED_SOP("security.violation.exception.no_sop_tls2"),
    FINISH_TITLE("finish"),
    INVALID_ADDRESS("invalid.address.exception.no_https"),
    INVALID_CERT("element.parsing.exception.invalid_cert_number"),
    INVALID_ELEMENT("invalid.tctoken.element.invalid_element"),
    INVALID_HTTP_STATUS("paos.exception.invalid_http_status_code"),
    INVALID_REDIRECT("redirect.cert.validator.invalid_redirect"),
    INVALID_REFRESH_ADDRESS("invalid.tctoken.element.invalid_refresh_address"),
    INVALID_REFRESH_ADDRESS_NOSOP("invalid.tctoken.element.invalid_refresh_address_nosop"),
    INVALID_RESULT_STATUS("invalid.result.status.exception"),
    INVALID_SIGNATURE_NUMBER("element.parsing.exception.invalid_signature_number"),
    INVALID_TCTOKEN_URL("invalid.tctoken.url.exception.invalid_tctoken_url"),
    INVALID_URL("illegal.argument.exception.invalid_url"),
    MALFORMED_TOKEN("invalid.tctoken.exception.malformed_tctoken"),
    MALFORMED_URL("invalid.tctoken.url.exception.malformed_url"),
    MARSHALLING_ERROR("dispatcher.exception.failed_jaxb_object_marshaling"),
    MAX_REDIRECTS("resource.exception.max_redirects_exceeded"),
    MESSAGE_ID_MISSMATCH("paos.exception.message_id_mismatch"),
    MISSING_ELEMENT("invalid.tctoken.element.missing_element"),
    MISSING_LOCATION_HEADER("resource.exception.no_location_header"),
    NO_ACTIVATION_PARAMETERS("missing.activation.parameter.exception.no_activation_parameters"),
    NO_HTTPS_URL("invalid.tctoken.url.exception.no_https_url"),
    NO_MESSAGE_ID("paos.exception.no_message_id"),
    NO_PARAMS("missing.activation.parameter.exception.no_suitable_parameters"),
    NO_REDIRECT_AVAILABLE("invalid.redirect.url.exception.no_url_available"),
    NO_REFRESH_ADDRESS("invalid.tctoken.element.no_refresh_address"),
    NO_RESPONSE_FROM_SERVER("paos.exception.no_response_from_server"),
    NO_SERVER_ADDRESS("invalid.tctoken.element.no_server_address"),
    NO_TCTOKEN_IN_DATA("invalid.tctoken.exception.no_tctoken"),
    NO_TOKEN("missing.activation.parameter.exception.no_valid_tctoken_available"),
    NO_URL("invalid.redirect.url.exception.no_url"),
    PAOS_CONNECTION_EXCEPTION("paos.connection.exception"),
    REDIRECT_MALFORMED_URL("redirect.cert.validator.malformed_subject_url"),
    REFRESH_DETERMINATION_FAILED("invalid.redirect.url.exception.refresh_address_determination_failed"),
    REFRESH_URL_ERROR("illegal.state.exception.invalid_refresh_address_in_tctoken"),
    REMOVE_CARD("remove_card_msg"),
    RETRIEVAL_FAILED("tctoken.retrieval.exception"),
    SOAP_MESSAGE_FAILURE("paos.exception.soap_message_creation_failed"),
    UNKNOWN_ECARD_ERROR("paos.exception.unknown_ecard_exception"),
    UNKNOWN_SEC_PROTOCOL("connection.error.unknown_sec_protocol"),
    UNSUPPORTED_FEATURE("illegal.argument.exception.unsupported_parser_feature"),
    WRONG_SERVER_RESULT("connection.error.invalid_status_code");

    private final String key;

    private ErrorTranslations(String key) {
	this.key = key;
    }

    @Override
    public String getKey() {
	return key;
    }

}
