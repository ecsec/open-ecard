/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import skid.mob.lib.NativeHttpClient;


/**
 *
 * @author Tobias Wich
 */
public class JavaHttpClient implements NativeHttpClient {

    private final String startUrl;
    private final Map<String, String> headers;

    private int resCode = -1;
    private InputStream resContent;
    private String finalUrl;

    JavaHttpClient(String url) {
	this.startUrl = url;
	this.headers = new HashMap<>();
    }

    @Override
    public void setHeader(String key, String value) {
	headers.put(key, value);
    }

    @Override
    public void performRequest() throws MalformedURLException, IOException {
	HttpURLConnection con = HttpURLConnection.class.cast(new URL(startUrl).openConnection());
	con.setInstanceFollowRedirects(true);

	for (Map.Entry<String, String> he : headers.entrySet()) {
	    con.setRequestProperty(he.getKey(), he.getValue());
	}

	con.connect();
	resCode = con.getResponseCode();
	if (resCode == HttpURLConnection.HTTP_OK) {
	    // get session id
	    resContent = con.getInputStream();
	    finalUrl = con.getURL().toString();
	}
    }

    @Override
    public int getResponseCode() {
	return resCode;
    }

    @Override
    public InputStream getContent() {
	return resContent;
    }

    @Override
    public String getFinalUrl() {
	return finalUrl;
    }

}
