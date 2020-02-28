/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.client;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import org.json.JSONObject;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skid.mob.impl.JsonConfig;


/**
 *
 * @author Tobias Wich
 */
public class SkidCApiClient {

    static {
	JsonConfig.assertInitialized();
    }

    private static final Logger LOG = LoggerFactory.getLogger(SkidCApiClient.class);

    private static final String BROKER_PATH = "broker/api/";
    private static final String OPTIONS_PATH = BROKER_PATH + "options";

    private final String skidBaseUri;

    public SkidCApiClient(String skidBaseUri) {
	this.skidBaseUri = skidBaseUri;
    }

    private URL makeUrl(String path) {
	UrlBuilder ub = getBuilder();
	ub = ub.addPathSegment(path);
	return buildUrl(ub);
    }

    private URL makeUrl(String path, Map<String, String> queryParams) {
	UrlBuilder ub = getBuilder();
	ub = ub.addPathSegment(path);
	ub = queryParams.entrySet().stream()
		.reduce(ub, (u, e) -> u.queryParamUrl(e.getKey(), e.getValue()), (id, u) -> u);
	return buildUrl(ub);
    }

    private UrlBuilder getBuilder() {
	try {
	    return UrlBuilder.fromUrl(skidBaseUri);
	} catch (URISyntaxException ex) {
	    LOG.error("Failed to create UrlBuilder instance due to invalid base URL.", ex);
	    throw new IllegalArgumentException("Failed to create UrlBuilder instance due to invalid base URL.", ex);
	}
    }

    private URL buildUrl(UrlBuilder ub) {
	try {
	    return ub.build().toURL();
	} catch (URISyntaxException | MalformedURLException ex) {
	    LOG.error("Failed to build Skid API URL.", ex);
	    throw new IllegalArgumentException("Failed to build Skid API URL.", ex);
	}
    }

    private static HttpURLConnection getClient(URL url) throws IOException {
	HttpURLConnection con = HttpURLConnection.class.cast(url.openConnection());
	return con;
    }

    public static String fetch(URL url, String mimeType) throws NetworkError, ServerError {
	try {
	    HttpURLConnection con = getClient(url);
	    con.setRequestProperty("Accept", mimeType);

	    con.connect();
	    int resCode = con.getResponseCode();
	    if (resCode == HttpURLConnection.HTTP_OK) {
		// get session id
		InputStream objStream = con.getInputStream();
		// TODO: read content encoding from header (Content-Type: application/json; charset=UTF-8
		String objString = FileUtils.toString(objStream, "UTF-8");

		return objString;
	    } else {
		throw new ServerError(resCode, "Failed to retrieve session from SAML FS.");
	    }
	} catch (IOException ex) {
	    throw new NetworkError("Failed execute HTTP request.", ex);
	}
    }

    private static Object toJson(String val) throws InvalidServerData {
	try {
	    Object jsonObj = Configuration.defaultConfiguration().jsonProvider().parse(val);
	    return jsonObj;
	} catch (InvalidJsonException ex) {
	    LOG.error("Data received from server is not valid JSON.", ex);
	    throw new InvalidServerData("Data received from server is not valid JSON.", ex);
	}
    }

    public Broker broker() {
	return new Broker();
    }

    public class Broker {

	public Object getOptions(String session) throws NetworkError, ServerError, InvalidServerData {
	    String objString = fetch(makeUrl(OPTIONS_PATH, Collections.singletonMap("session", session)), "application/json");
	    return toJson(objString);
	}

    }

}
