/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.client.model;

import com.jayway.jsonpath.DocumentContext;
import static com.jayway.jsonpath.Filter.*;
import static com.jayway.jsonpath.Criteria.*;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 * @author Tobias Wich
 */
public class UiInfo {

    private final DocumentContext jsonCtx;

    public UiInfo(JSONArray jsonObj) {
	this.jsonCtx = JsonPath.parse(jsonObj);
    }

    public String displayName() {
	return displayName("en");
    }

    public String displayName(String lang) {
	return extractValue(fetchBestValue("DisplayName", lang));
    }

    public String informationUrl() {
	return informationUrl("en");
    }

    public String informationUrl(String lang) {
	return extractValue(fetchBestValue("InformationURL", lang));
    }

    public String privacyStatementUrl() {
	return privacyStatementUrl("en");
    }

    public String privacyStatementUrl(String lang) {
	return extractValue(fetchBestValue("PrivacyStatementURL", lang));
    }

    public String logoUrl() {
	return logoUrl("en");
    }

    public String logoUrl(String lang) {
	// TODO: get proper logo object
	return extractValue(fetchBestValue("Logo", lang));
    }

    private JSONObject fetchBestValue(String objName, String lang) {
	// try non english
	if (lang != null && ! lang.isEmpty() && ! lang.equalsIgnoreCase("en")) {
	    JSONObject result = fetchValue(objName, lang);
	    if (result != null) {
		return result;
	    } else {
		lang = "en";
	    }
	}

	// try english if requested
	if (lang != null && lang.equalsIgnoreCase("en")) {
	    JSONObject result = fetchValue(objName, lang);
	    if (result != null) {
		return result;
	    }
	}

	// try wildcard
	JSONObject result = fetchValue(objName, null);
	return result;
    }

    private JSONObject fetchValue(String objName, String lang) {
	Filter filter = filter(where("value").exists(true));
	if (lang != null && ! lang.isEmpty()) {
	    lang = lang.toLowerCase();
	    Filter langFilter = filter(where("lang").eq(lang));
	    filter = filter.and(langFilter);
	}

	String path = String.format("$[?(@.JAXBElement.name == '{urn:oasis:names:tc:SAML:metadata:ui}%s')].JAXBElement.value[?]", objName);
	JSONArray objs = jsonCtx.read(path, filter);
	JSONObject obj = objs.optJSONObject(0);

	return obj;
    }

    private String extractValue(JSONObject obj) {
	String result = null;
	if (obj != null) {
	    result = obj.optString("value");
	}
	return result;
    }

}
