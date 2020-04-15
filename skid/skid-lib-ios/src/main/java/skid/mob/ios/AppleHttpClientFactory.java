/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.ios;

import org.robovm.apple.foundation.NSHTTPCookieStorage;
import org.robovm.apple.foundation.NSURLSession;
import org.robovm.apple.foundation.NSURLSessionConfiguration;
import skid.mob.lib.NativeHttpClient;
import skid.mob.lib.NativeHttpClientFactory;


/**
 *
 * @author Tobias Wich
 */
public class AppleHttpClientFactory implements NativeHttpClientFactory {

    @Override
    public NativeHttpClient forUrl(String url) {
	NSURLSessionConfiguration shared = NSURLSession.getSharedSession().getConfiguration();
	NSHTTPCookieStorage cookies = shared.getHTTPCookieStorage();
	return new AppleHttpClient(url, cookies);
    }

}
