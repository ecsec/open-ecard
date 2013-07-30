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
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ExecutePACEResponse {

    private static final Logger logger = LoggerFactory.getLogger(ExecutePACEResponse.class);

    final int result;
    final short length;
    final byte[] data;

    public ExecutePACEResponse(byte[] response) {
	result = ByteUtils.toInteger(Arrays.copyOfRange(response, 0, 4));
	length = ByteUtils.toShort(new byte[] { response[5], response[4] });
	data   = Arrays.copyOfRange(response, 6, 6 + length);
    }

    public boolean isError() {
	return result != 0;
    }

    public int getResultCode() {
	return result;
    }

    public Result getResult() {
	if (result == 0x00000000) return WSHelper.makeResultOK();
	// errors in input
	if (result ==  0xD0000001) return WSHelper.makeResultUnknownError("Inconsistent lengths in input.");
	if (result ==  0xD0000002) return WSHelper.makeResultUnknownError("Unexpected data in input.");
	if (result ==  0xD0000003) return WSHelper.makeResultUnknownError("Unexpected combination of data in input.");
	// errors in protocol
	if (result ==  0xE0000001) return WSHelper.makeResultUnknownError("Syntax error in TLV response.");
	if (result ==  0xE0000002) return WSHelper.makeResultUnknownError("Unexpected or missing object in TLV response.");
	if (result ==  0xE0000003) return WSHelper.makeResultUnknownError("Unknown PIN-ID.");
	if (result ==  0xE0000006) return WSHelper.makeResultUnknownError("Wrong Authentication Token.");
	// Response APDU of the card reports error
	if ((result & 0xFFFC0000) == 0xF0000000) {
	    byte[] sw = new byte[]{(byte) ((result >> 8) & 0xFF), (byte) (result & 0xFF)};
	    String msg = CardCommandStatus.getMessage(sw);
	    int type = (result >> 16) & 0xFFFF;
	    if (type == 0xF000) return WSHelper.makeResultUnknownError("Select EF.CardAccess: " + msg);
	    if (type == 0xF001) return WSHelper.makeResultUnknownError("Read Binary EF.CardAccess: " + msg);
	    if (type == 0xF002) return WSHelper.makeResultUnknownError("MSE Set AT: " + msg);
	    if (type == 0xF003) return WSHelper.makeResultUnknownError("General Authenticat Step 1-4: " + msg);
	}
	// Others
	if (result ==  0xF0100001) return WSHelper.makeResultUnknownError("Communication abort.");
	if (result ==  0xF0100002) return WSHelper.makeResultError(ECardConstants.Minor.IFD.NO_CARD, "No card.");
	if (result ==  0xF0200001) return WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "Abort.");
	if (result ==  0xF0200002) return WSHelper.makeResultError(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Timeout.");
	// unknown error
	String hexStringResult = ByteUtils.toHexString(IntegerUtils.toByteArray(result));
	logger.warn("Unknown error in ExecutePACEResponse: {}", hexStringResult);
	return WSHelper.makeResultUnknownError(null);
    }

    public byte[] getData() {
	return data;
    }

}
