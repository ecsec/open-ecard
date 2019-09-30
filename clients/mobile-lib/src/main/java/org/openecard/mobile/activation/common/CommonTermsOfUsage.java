/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
 ************************************************************************** */
package org.openecard.mobile.activation.common;

import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import org.openecard.mobile.activation.TermsOfUsage;

/**
 *
 * @author Neil Crossley
 */
public class CommonTermsOfUsage implements TermsOfUsage {

    /**
     * One of {@code application/pdf}, {@code text/html}, or {@code text/plain}
     */
    protected String mimeType;
    protected byte[] data;

    public CommonTermsOfUsage(@Nonnull String mimeType, @Nonnull byte[] data) {
	this.mimeType = mimeType;
	this.data = data;
    }

    @Override
    public String getMimeType() {
	return mimeType;
    }

    /**
     * Retrieve raw bytes of the terms of usage document.
     * This method should be used when the document is a PDF.
     *
     * @return
     */
    public byte[] getDataBytes() {
	return data;
    }

    /**
     * Retrieve the terms of usage document as a string.
     * This method should be used when the document is a HTML or plain text.
     *
     * @return
     */
    @Override
    public String getDataString() {
	return new String(data, StandardCharsets.UTF_8);
    }

    public boolean isPdf() {
	return "application/pdf".equals(mimeType);
    }

    public boolean isHtml() {
	return "text/html".equals(mimeType);
    }

    public boolean isText() {
	return "text/plain".equals(mimeType);
    }

}
