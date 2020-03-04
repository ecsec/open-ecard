/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl.client.model;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;
import skid.mob.impl.client.InvalidServerData;


/**
 *
 * @author Tobias Wich
 */
public class AuthOption {

    private final DocumentContext jsonCtx;
    private AuthOptions derived;

    public AuthOption(JSONObject jsonObj) {
	this.jsonCtx = JsonPath.parse(jsonObj);
    }

    public void load() throws InvalidServerData {
	// validate content
	JSONArray derivedArray = jsonCtx.read("$.DerivedCredential.*");
	if (derivedArray.length() > 0) {
	    derived = new AuthOptions(derivedArray);
	    derived.load();
	}
    }
    
    public String optionId() {
	String val = jsonCtx.read("$.AuthenticationOptionID");
	return val;
    }

    public String type() {
	String val = jsonCtx.read("$.Type");
	return val;
    }

    public String protocol() {
	String val = jsonCtx.read("$.Protocol");
	return val;
    }

    public String issuer() {
	String val = jsonCtx.read("$.Issuer");
	return val;
    }

    public String activationType() {
	String val = jsonCtx.read("$.ActivationType");
	return val;
    }

    public String activateUrl() {
	String val = jsonCtx.read("$.ActivateURL");
	return val;
    }

    public UiInfo uiInfo() {
	JSONArray uiItemArray = jsonCtx.read("$.UIInfo.displayNameOrDescriptionOrKeywords.*");
	if (uiItemArray.length() > 0) {
	    UiInfo uii = new UiInfo(uiItemArray);
	    return uii;
	} else {
	    return null;
	}
    }

    public String authContextClassRef() {
	String val = jsonCtx.read("$.AuthnContextClassRef");
	return val;
    }

    public AuthOptions derivedCredentials() {
	return derived;
    }

    // TODO: AuthnContextDeclRef
    // TODO: Attribute
    // TODO: RequestedAttribute

}
