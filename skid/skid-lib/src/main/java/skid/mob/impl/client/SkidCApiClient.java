/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl.client;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skid.mob.lib.AttributeSelection;


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
    private static final String OPTIONS_SELECT_PATH = OPTIONS_PATH + "/select";
    private static final String OPTIONS_CANCEL_PATH = OPTIONS_PATH + "/cancel";

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

    public static String fetch(String method, URL url, String reqMimeType, String payload, String resMimeType) throws NetworkError, ServerError {
	boolean doSend = reqMimeType != null && payload != null;
	boolean doReceive = resMimeType != null;

	try {
	    HttpURLConnection con = getClient(url);

	    if (doSend) {
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", reqMimeType + "; charset=UTF-8");
	    }

	    if (doReceive) {
		con.setRequestProperty("Accept", resMimeType);
	    }

	    con.setRequestMethod(method);
	    con.connect();

	    // send payload
	    if (doSend) {
		try (OutputStream out = con.getOutputStream()) {
		    assert(payload != null); // not null due to doInput boolean
		    out.write(payload.getBytes("UTF-8"));
		}
	    }

	    int resCode = con.getResponseCode();
	    if (doReceive && resCode == HttpURLConnection.HTTP_OK) {
		// get session id
		try (InputStream objStream = con.getInputStream()) {
		    // TODO: read content encoding from header (Content-Type: application/json; charset=UTF-8
		    String objString = FileUtils.toString(objStream, "UTF-8");

		    return objString;
		}
	    } else if (! doReceive && resCode == HttpURLConnection.HTTP_NO_CONTENT) {
		return null;
	    } else if (resCode == HttpURLConnection.HTTP_NOT_FOUND) {
		throw new NotFound("The requested resource does not exist on the server.");
	    } else {
		throw new ServerError(resCode, "Failed to retrieve resource from SkIDentity server.");
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

    private String formUrlEncode(Map<String,String> formData) {
	try {
	    StringBuilder sb = new StringBuilder();
	    boolean first = true;
	    for (Map.Entry<String, String> e : formData.entrySet()) {
		String k = e.getKey();
		String v = URLEncoder.encode(e.getValue(), "UTF_8");

		if (first) {
		    first = false;
		} else {
		    sb.append("&");
		}
		sb.append(k);
		sb.append("=");
		sb.append(v);
	    }
	    return sb.toString();
	} catch (UnsupportedEncodingException ex) {
	    throw new UnsupportedOperationException("UTF-8 encoding is missing, there must be something wrong with the platform.");
	}
    }

    private String stringifySelection(List<AttributeSelection> selection) {
	StringBuilder s = new StringBuilder();
	s.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
	s.append("<RequestedAttributes xmlns=\"urn:oasis:names:tc:SAML:profile:privacy\" xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\">");

	for (AttributeSelection a : selection) {
	    if (a.isSelected()) {
		s.append("<md:RequestedAttribute Name=\"");
		// TODO: escape name, so it doesn't break the parse process
		s.append(a.getName());
		s.append("\" isRequired=\"");
		s.append(a.isRequired());
		s.append("\"></md:RequestedAttribute>");
	    }
	}

	s.append("</RequestedAttributes>");

	return s.toString();
    }

    public Broker broker() {
	return new Broker();
    }

    public class Broker {

	public Object getOptions(String session) throws NetworkError, NotFound, ServerError, InvalidServerData {
	    String objString = fetch("GET", makeUrl(OPTIONS_PATH, Collections.singletonMap("session", session)), null, null, "application/json");
	    return toJson(objString);
	}

	public String selectOption(String session, String optionId, List<AttributeSelection> selection) throws NetworkError, NotFound, ServerError {
	    HashMap<String, String> formData = new HashMap<>();
	    formData.put("session", session);
	    formData.put("option", optionId);
	    formData.put("user-selection", stringifySelection(selection));
	    String formDataEnc = formUrlEncode(formData);

	    String activateUrl = fetch("POST", makeUrl(OPTIONS_SELECT_PATH), "application/x-www-form-urlencoded", formDataEnc, "text/plain");
	    return activateUrl;
	}

	public String cancelSession(String session) throws NetworkError, NotFound, ServerError {
	    HashMap<String, String> formData = new HashMap<>();
	    formData.put("session", session);
	    String formDataEnc = formUrlEncode(formData);

	    String finishUrl = fetch("POST", makeUrl(OPTIONS_CANCEL_PATH), "application/x-www-form-urlencoded", formDataEnc, "text/plain");
	    return finishUrl;
	}

    }

}
