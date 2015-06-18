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

package org.openecard.common.apdu.common;

import javax.annotation.Nonnull;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;


/**
 * APDU Template function capable of creating TLV BER data based on a tag and a value.
 *
 * @author Tobias Wich
 */
public class TLVFunction implements APDUTemplateFunction {

    @Override
    @Nonnull
    public String call(Object... params) throws APDUTemplateException {
	if (params.length != 2) {
	    throw new APDUTemplateException("Invalid number of parameters given. Two are needed.");
	}
	byte[] tag = makeBytes(params[0]);
	byte[] value = makeBytes(params[1]);

	try {
	    TLV tlv = new TLV();
	    tlv.setTagNumWithClass(tag);
	    tlv.setValue(value);

	    byte[] result = tlv.toBER();
	    String resultStr = ByteUtils.toHexString(result);
	    return resultStr;
	} catch (TLVException ex) {
	    throw new APDUTemplateException("Failed to create TLV structure based on given parameters.", ex);
	}
    }

    private byte[] makeBytes(Object o) {
	if (o instanceof String) {
	    return StringUtils.toByteArray((String) o, true);
	} else {
	    return (byte[]) o;
	}
    }

}
