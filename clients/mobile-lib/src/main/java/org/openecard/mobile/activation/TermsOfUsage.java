/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation;

import org.openecard.robovm.annotations.FrameworkInterface;

/**
 *
 * @author Neil Crossley
 */
@FrameworkInterface
public interface TermsOfUsage {

    /**
     * Retrieve raw bytes of the terms of usage document.
     * This method should be used when the document is a PDF.
     *
     * @return
     */
    byte[] getDataBytes();

    /**
     * Retrieve the terms of usage document as a string.
     * This method should be used when the document is a HTML or plain text.
     *
     * @return
     */
    String getDataString();

    String getMimeType();

    boolean isHtml();

    boolean isPdf();

    boolean isText();

}
