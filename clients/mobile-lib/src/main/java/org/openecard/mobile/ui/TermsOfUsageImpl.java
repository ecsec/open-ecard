/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.mobile.ui;

import java.nio.charset.StandardCharsets;
import org.openecard.mobile.activation.TermsOfUsage;


/**
 *
 * @author Tobias Wich
 */
class TermsOfUsageImpl implements TermsOfUsage {

    private final byte[] data;
    private final String mimeType;

    public TermsOfUsageImpl(String mimeType, byte[] data) {
	this.data = data;
	this.mimeType = mimeType;
    }

//    @Override
//    public byte[] getDataBytes() {
//	return data;
//    }

    @Override
    public String getDataString() {
	return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public String getMimeType() {
	return mimeType;
    }

    @Override
    public boolean isHtml() {
	return "text/html".equals(mimeType);
    }

    @Override
    public boolean isPdf() {
	return "application/pdf".equals(mimeType);
    }

    @Override
    public boolean isText() {
	return "text/plain".equals(mimeType);
    }

}
