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
import java.util.HashMap;
import java.util.Map;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich
 */
public class PCSCFeatures {

    public static final int VERIFY_PIN_START         = 0x01;
    public static final int VERIFY_PIN_FINISH        = 0x02;
    public static final int MODIFY_PIN_START         = 0x03;
    public static final int MODIFY_PIN_FINISH        = 0x04;
    public static final int GET_KEY_PRESSED          = 0x05;
    public static final int VERIFY_PIN_DIRECT        = 0x06;
    public static final int MODIFY_PIN_DIRECT        = 0x07;
    public static final int MCT_READER_DIRECT        = 0x08;
    public static final int MCT_UNIVERSAL            = 0x09;
    public static final int IFD_PIN_PROPERTIES       = 0x0A;
    public static final int ABORT                    = 0x0B;
    public static final int SET_SPE_MESSAGE          = 0x0C;
    public static final int VERIFY_PIN_DIRECT_APP_ID = 0x0D;
    public static final int MODIFY_PIN_DIRECT_APP_ID = 0x0E;
    public static final int WRITE_DISPLAY            = 0x0F;
    public static final int GET_KEY                  = 0x10;
    public static final int IFD_DISPLAY_PROPERTIES   = 0x11;
    public static final int GET_TLV_PROPERTIES       = 0x12;
    public static final int CCID_ESC_COMMAND         = 0x13;
    public static final int EXECUTE_PACE             = 0x20;


    public static int GET_FEATURE_REQUEST_CTLCODE() {
	return SCARD_CTL_CODE(3400);
    }

    private static int SCARD_CTL_CODE(int code) {
	if (isWindows()) {
	    return (0x31 << 16 | code << 2);
	} else {
	    return 0x42000000 + code;
	}
    }

    private static boolean isWindows() {
	String osName = System.getProperty("os.name").toLowerCase();
	return (osName.contains("windows"));
    }


    public static Map<Integer,Integer> featureMapFromRequest(byte[] featureResponse) {
	HashMap<Integer,Integer> result = new HashMap<>();

	if ((featureResponse.length % 6) == 0) {
	    int i = 0;
	    while (i < featureResponse.length) {
		byte[] nextChunk = Arrays.copyOfRange(featureResponse, i, i+6);
		if (nextChunk.length == 6 && nextChunk[1] == 4) {
		    byte tag = nextChunk[0];
		    byte[] codeData = Arrays.copyOfRange(nextChunk, 2, 6);
		    int code = ByteUtils.toInteger(codeData);
		    result.put(Integer.valueOf(tag), code);
		}
		i += 6;
	    }
	}

	return result;
    }

}
