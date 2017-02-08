/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.openecard.mdlw.sal.struct.CkAttribute;


/**
 *
 * @author Tobias Wich
 */
public class AttributeUtils {

    @Nullable
    public static byte[] getBytes(CkAttribute raw) {
	int dataLen = raw.getLength().intValue();
	if (dataLen > 0) {
	    return raw.getData().getByteArray(0, dataLen);
	} else {
	    return new byte[0];
	}
    }

    @Nullable
    public static String getString(CkAttribute raw) {
	int dataLen = raw.getLength().intValue();
	if (dataLen > 0) {
	    byte[] rawData = raw.getData().getByteArray(0, dataLen);
	    return new String(rawData, StandardCharsets.UTF_8).trim();
	} else {
	    return null;
	}
    }

    @Nullable
    public static Boolean getBool(CkAttribute raw) {
	int dataLen = raw.getLength().intValue();
	if (dataLen > 0) {
	    return raw.getData().getByte(0) == 0 ? Boolean.FALSE : Boolean.TRUE;
	} else {
	    return null;
	}
    }

    @Nullable
    public static long getLong(CkAttribute raw) {
	int dataLen = raw.getLength().intValue();
	if (dataLen > 0) {
	    if (dataLen == 4) {
		return raw.getData().getInt(0);
	    } else {
		return raw.getData().getLong(0);
	    }
	} else {
	    return -1;
	}
    }

    @Nullable
    public static long[] getLongs(CkAttribute raw) {
	int dataLen = raw.getLength().intValue();
	if (dataLen > 0) {
	    return raw.getData().getLongArray(0, dataLen / 8);
	} else {
	    return new long[0];
	}
    }

}
