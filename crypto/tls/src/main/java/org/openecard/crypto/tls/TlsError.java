/****************************************************************************
 * Copyright (C) 2015-2017 ecsec GmbH.
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

package org.openecard.crypto.tls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.tls.AlertDescription;
import org.openecard.bouncycastle.tls.AlertLevel;


/**
 * Data class containing one error which happened in the TLS stack.
 * The error class can be used for received and sent errors. It's main purpose is to create a printable representation
 * of the error for logging purposes.
 *
 * @author Tobias Wich
 */
public class TlsError {

    private final short alertLevel;
    private final short alertDescription;
    private final String message;
    private final Throwable cause;

    public TlsError(short alertLevel, short alertDescription) {
	this(alertLevel, alertDescription, null, null);
    }

    public TlsError(short alertLevel, short alertDescription, @Nullable String message, @Nullable Throwable cause) {
	this.alertLevel = alertLevel;
	this.alertDescription = alertDescription;
	this.message = message;
	this.cause = cause;
    }

    public short getAlertLevel() {
	return alertLevel;
    }

    public String getAlertLevelStr() {
	switch (getAlertLevel()) {
	    case AlertLevel.fatal:
		return "FATAL";
	    case AlertLevel.warning:
		return "WARN";
	    default:
		return "UNKNOWN";
	}
    }

    public short getAlertDescription() {
	return alertDescription;
    }

    public String getAlertDescriptionStr() {
	// TODO: localize
	switch (alertDescription) {
	    case AlertDescription.close_notify:
		return String.format("Close [close_notify=%d]", alertDescription);
	    case AlertDescription.unexpected_message:
		return String.format("Unexpected message [unexpected_message=%d]", alertDescription);
	    case AlertDescription.bad_record_mac:
		return String.format("MAC of record invalid [bad_record_mac=%d]", alertDescription);
	    case AlertDescription.decryption_failed:
		return String.format("Decryption failed [decryption_failed=%d]", alertDescription);
	    case AlertDescription.record_overflow:
		return String.format("Overly large record received [record_overflow=%d]", alertDescription);
	    case AlertDescription.decompression_failure:
		return String.format("Decompression failed [decompression_failure=%d]", alertDescription);
	    case AlertDescription.handshake_failure:
		return String.format("No acceptable set of security parameters found [handshake_failure=%d]", alertDescription);
	    case AlertDescription.no_certificate:
		return String.format("No certificate [no_certificate=%d]", alertDescription);
	    case AlertDescription.bad_certificate:
		return String.format("Certificate corrupt [bad_certificate=%d]", alertDescription);
	    case AlertDescription.unsupported_certificate:
		return String.format("Certificate type not supported [unsupported_certificate=%d]", alertDescription);
	    case AlertDescription.certificate_revoked:
		return String.format("Certificate revoked [certificate_revoked=%d]", alertDescription);
	    case AlertDescription.certificate_expired:
		return String.format("Certificate expired [certificate_expired=%d]", alertDescription);
	    case AlertDescription.certificate_unknown:
		return String.format("Certificate invalid [certificate_unknown=%d]", alertDescription);
	    case AlertDescription.illegal_parameter:
		return String.format("Illegal parameter in handshake [illegal_parameter=%d]", alertDescription);
	    case AlertDescription.unknown_ca:
		return String.format("No trust anchor found [unknown_ca=%d]", alertDescription);
	    case AlertDescription.access_denied:
		return String.format("Access denied [access_denied=%d]", alertDescription);
	    case AlertDescription.decode_error:
		return String.format("Message decoding error [decode_error=%d]", alertDescription);
	    case AlertDescription.decrypt_error:
		return String.format("Handshake operation failed [decrypt_error=%d]", alertDescription);
	    case AlertDescription.export_restriction:
		return String.format("Export restriction [export_restriction=%d]", alertDescription);
	    case AlertDescription.protocol_version:
		return String.format("No common protocol version [protocol_version=%d]", alertDescription);
	    case AlertDescription.insufficient_security:
		return String.format("Security requirements not met [insufficient_security=%d]", alertDescription);
	    case AlertDescription.internal_error:
		return String.format("Internal error [internal_error=%d]", alertDescription);
	    case AlertDescription.user_canceled:
		return String.format("User cancellation [user_canceled=%d]", alertDescription);
	    case AlertDescription.no_renegotiation:
		return String.format("Renegotiation support missing [no_renegotiation=%d]", alertDescription);
	    case AlertDescription.unsupported_extension:
		return String.format("Unsupported extension received [unsupported_extension=%d]", alertDescription);
	    case AlertDescription.certificate_unobtainable:
		return String.format("Bad client certificate [certificate_unobtainable=%d]", alertDescription);
	    case AlertDescription.unrecognized_name:
		return String.format("SNI name unknown [unrecognized_name=%d]", alertDescription);
	    case AlertDescription.bad_certificate_status_response:
		return String.format("Client certificate invalid [bad_certificate_status_response=%d]", alertDescription);
	    case AlertDescription.bad_certificate_hash_value:
		return String.format("Client certificate hash invalid [bad_certificate_hash_value=%d]", alertDescription);
	    case AlertDescription.unknown_psk_identity:
		return String.format("PSK identity unkown [unknown_psk_identity=%d]", alertDescription);
	    default:
		return String.format("Unknown alert (%d)", alertDescription);
	}
    }

    @Nonnull
    public String getMessage() {
	if (message == null) {
	    Throwable causeTmp = getCause();
	    if (causeTmp == null) {
		return "Unknown error.";
	    } else {
		return causeTmp.getMessage();
	    }
	} else {
	    return message;
	}
    }

    @Nullable
    public Throwable getCause() {
	return cause;
    }

    @Override
    public String toString() {
	return String.format("TLS(%s): %s --> %s", getAlertLevelStr(), getAlertDescriptionStr(), getMessage());
    }

}
