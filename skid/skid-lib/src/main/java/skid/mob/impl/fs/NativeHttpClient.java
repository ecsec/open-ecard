/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl.fs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import org.openecard.robovm.annotations.FrameworkInterface;

/**
 *
 * @author Tobias Wich
 */
@FrameworkInterface
public interface NativeHttpClient {

    void setHeader(String key, String value);
    void performRequest() throws MalformedURLException, SocketTimeoutException, IOException;
    InputStream getContent();

    int getResponseCode();
    String getFinalUrl();

}
