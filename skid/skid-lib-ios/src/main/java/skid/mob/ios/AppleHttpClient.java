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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import org.openecard.common.util.Promise;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSHTTPCookieStorage;
import org.robovm.apple.foundation.NSHTTPURLResponse;
import org.robovm.apple.foundation.NSMutableURLRequest;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.foundation.NSURLResponse;
import org.robovm.apple.foundation.NSURLSession;
import org.robovm.apple.foundation.NSURLSessionConfiguration;
import org.robovm.apple.foundation.NSURLSessionDataTask;
import org.robovm.objc.block.VoidBlock3;
import skid.mob.lib.NativeHttpClient;


/**
 *
 * @author Tobias Wich
 */
public class AppleHttpClient implements NativeHttpClient {

    private final String startUrl;
    private final NSURLSession sess;
    private final NSMutableURLRequest req;

    private int resCode = -1;
    private InputStream resContent;
    private String finalUrl;

    private volatile IOException exIo;

    AppleHttpClient(String url, NSHTTPCookieStorage cookies) {
	this.startUrl = url;

	NSURLSessionConfiguration conf = NSURLSessionConfiguration.getDefaultSessionConfiguration();
	conf.setHTTPCookieStorage(cookies);
	sess = new NSURLSession(conf);

	req = new NSMutableURLRequest();
    }

    @Override
    public void setHeader(String key, String value) {
	req.setHTTPHeaderField(key, value);
    }

    @Override
    public void performRequest() throws MalformedURLException, SocketTimeoutException, IOException {
	try {
	    req.setURL(new NSURL(startUrl));

	    ResponseCallback rc = new ResponseCallback();
	    NSURLSessionDataTask task = sess.newDataTask(req, rc);
	    task.resume();
	    rc.awaitResponse();

	    if (exIo != null) {

	    }

	} catch (InterruptedException ex) {
	    throw new SocketTimeoutException(ex.getMessage());
	}
    }

    @Override
    public InputStream getContent() {
	return resContent;
    }

    @Override
    public int getResponseCode() {
	return resCode;
    }

    @Override
    public String getFinalUrl() {
	return finalUrl;
    }

    private class ResponseCallback implements VoidBlock3<NSData, NSURLResponse, NSError> {

	private Promise p = new Promise();

	@Override
	public void invoke(NSData data, NSURLResponse resp, NSError error) {
	    try {
		if (error == null) {
		    NSHTTPURLResponse hr = (NSHTTPURLResponse) resp;

		    resCode = (int) hr.getStatusCode();
		    finalUrl = hr.getURL().getAbsoluteString();

		    resContent = new ByteArrayInputStream(data.getBytes());
		} else {
		    // error case
		    exIo = new IOException(error.description());
		}
	    } finally {
		p.deliver(1);
	    }
	}

	void awaitResponse() throws InterruptedException {
	    p.deref();
	}

    }

}
