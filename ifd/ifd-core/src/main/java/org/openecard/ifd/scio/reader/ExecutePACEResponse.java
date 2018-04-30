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

package org.openecard.ifd.scio.reader;

import java.util.Arrays;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.common.CardCommandStatus;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.IntegerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class ExecutePACEResponse {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutePACEResponse.class);

    final int result;
    final short length;
    final byte[] data;

    public ExecutePACEResponse(byte[] response) {
	result = ByteUtils.toInteger(Arrays.copyOfRange(response, 0, 4), false);
	length = ByteUtils.toShort(Arrays.copyOfRange(response, 4, 6), false);
	data   = Arrays.copyOfRange(response, 6, 6 + length);
    }

    public boolean isError() {
	return result != 0;
    }

    public int getResultCode() {
	return result;
    }

    public Result getResult() {
	switch (result) {
	    case 0x00000000: return WSHelper.makeResultOK();
	    // errors in input
	    case 0xD0000001: return WSHelper.makeResultUnknownError("Inconsistent lengths in input.");
	    case 0xD0000002: return WSHelper.makeResultUnknownError("Unexpected data in input.");
	    case 0xD0000003: return WSHelper.makeResultUnknownError("Unexpected combination of data in input.");
	    // errors in protocol
	    case 0xE0000001: return WSHelper.makeResultUnknownError("Syntax error in TLV response.");
	    case 0xE0000002: return WSHelper.makeResultUnknownError("Unexpected or missing object in TLV response.");
	    case 0xE0000003: return WSHelper.makeResultUnknownError("Unknown PIN-ID.");
	    case 0xE0000006: return WSHelper.makeResultUnknownError("Wrong Authentication Token.");
	    // Others
	    case 0xF0100001: return WSHelper.makeResultUnknownError("Communication abort.");
	    case 0xF0100002: return WSHelper.makeResultError(ECardConstants.Minor.IFD.Terminal.NO_CARD, "No card.");
	    case 0xF0200001: return WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "Abort.");
	    case 0xF0200002: return WSHelper.makeResultError(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Timeout.");
	    // Response APDU of the card reports error
	    default: {
                byte[] sw = new byte[]{(byte) ((result >> 8) & 0xFF), (byte) (result & 0xFF)};
		String msg = CardCommandStatus.getMessage(sw);
		int type = (result >> 16) & 0xFFFF;
		if ((result & 0xFFFC0000) == 0xF0000000) {
		    switch (type) {
			case 0xF000: return WSHelper.makeResultUnknownError("Select EF.CardAccess: " + msg);
			case 0xF001: return WSHelper.makeResultUnknownError("Read Binary EF.CardAccess: " + msg);
			case 0xF002: return WSHelper.makeResultUnknownError("MSE Set AT: " + msg);
			case 0xF003: return WSHelper.makeResultUnknownError("General Authenticat Step 1-4: " + msg);
		    }
		} else if ((result & 0xFFFC0000) == 0xf0040000) {
		    // codes for wrong PIN
                    switch (ByteUtils.toInteger(sw))  {
			case 0x6300: return WSHelper.makeResultError(ECardConstants.Minor.IFD.AUTHENTICATION_FAILED, msg);
                        case 0x63C0: return WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORD_BLOCKED, msg);
                        case 0x63C1: return WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORD_SUSPENDED, msg);
                        case 0x63C2: return WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORD_ERROR, msg);
                        case 0x6283: return WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORD_DEACTIVATED, msg);
                    }
                }

		// unknown error
		String hexStringResult = ByteUtils.toHexString(IntegerUtils.toByteArray(result));
		LOG.warn("Unknown error in ExecutePACEResponse: {}", hexStringResult);
		return WSHelper.makeResultUnknownError(null);
	    }
	}
    }

    public byte[] getData() {
	return data;
    }

}
