/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.common.apdu.exception;

import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import javax.annotation.Nullable;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.common.CardResponseAPDU;


/**
 * @author Moritz Horsch
 */
public final class APDUException extends ECardException {

    private static final long serialVersionUID = 1L;

    private TransmitResponse transmitResponse;
    private CardResponseAPDU responseAPDU;

    /**
     * Creates a new APDUException.
     *
     * @param msg Message
     */
    public APDUException(String msg) {
	makeException(this, msg);
    }

    /**
     * Creates a new APDUException.
     *
     * @param minor Minor message
     * @param msg Message
     */
    public APDUException(String minor, String msg) {
	makeException(this, minor, msg);
    }

    /**
     * Creates a new APDUException.
     *
     * @param r Result
     */
    public APDUException(Result r) {
	makeException(this, r);
    }

    /**
     * Creates a new APDUException.
     *
     * @param cause Cause
     */
    public APDUException(Throwable cause) {
	makeException(this, cause);
    }
    
    /**
     * Creates a new APDUException.
     *
     * @param ex WSException
     */
    public APDUException(WSHelper.WSException ex) {
	makeException(this, ex, ex.getResultMajor(), ex.getResultMinor(), ex.getResultMessage());
    }

    /**
     * Creates a new APDUException.
     *
     * @param cause Cause
     * @param tr TransmitResponse
     */
    public APDUException(Throwable cause, TransmitResponse tr) {
	this(cause);

	transmitResponse = tr;
	if (! tr.getOutputAPDU().isEmpty()) {
	    responseAPDU = new CardResponseAPDU(tr);
	}
    }

    /**
     * Creates a new APDUException.
     *
     * @param ex WSException
     * @param tr TransmitResponse
     */
    public APDUException(WSHelper.WSException ex, TransmitResponse tr) {
	this(ex);

	transmitResponse = tr;
	if (! tr.getOutputAPDU().isEmpty()) {
	    responseAPDU = new CardResponseAPDU(tr);
	}
    }

    /**
     * Returns the TransmitResponse.
     *
     * @return TransmitResponseTransmitResponse
     */
    public TransmitResponse getTransmitResponse() {
	return transmitResponse;
    }

    /**
     * Returns the ResponseAPDU.
     *
     * @return ResponseAPDU
     */
    @Nullable
    public CardResponseAPDU getResponseAPDU() {
	return responseAPDU;
    }

}
