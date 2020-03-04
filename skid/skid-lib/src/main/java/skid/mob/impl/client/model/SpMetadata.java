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
import skid.mob.impl.client.InvalidServerData;


/**
 *
 * @author Tobias Wich
 */
public class SpMetadata {

    private final DocumentContext jsonCtx;

    boolean loaded = false;
    private UiInfo uii;
    private AuthOptions authOpts;

    public SpMetadata(Object jsonObj) {
	this.jsonCtx = JsonPath.parse(jsonObj);
    }

    public synchronized void load() throws InvalidServerData {
	if (! loaded) {
	    JSONArray uiItemArray = jsonCtx.read("$.value.roleDescriptorOrIDPSSODescriptorOrSPSSODescriptor[0].SPSSODescriptor.Extensions.any[?(@.name == '{urn:oasis:names:tc:SAML:metadata:ui}UIInfo')].value.displayNameOrDescriptionOrKeywords.*");
	    if (uiItemArray.length() < 1) {
		throw new InvalidServerData("No UIInfo object present in options structure.");
	    }
	    uii = new UiInfo(uiItemArray);

	    JSONArray authOptsArray = jsonCtx.read("$.value.roleDescriptorOrIDPSSODescriptorOrSPSSODescriptor[0].SPSSODescriptor.Extensions.any[?(@.name == '{urn:oasis:names:tc:SAML:profile:privacy}Accepts')].value.AuthenticationOption.*");
	    if (authOptsArray.length() < 1) {
		throw new InvalidServerData("No Accepts object present in options structure.");
	    }
	    authOpts = new AuthOptions(authOptsArray);
	    authOpts.load();

	    loaded = true;
	}
    }

    public UiInfo getUiInfo() {
	return uii;
    }

    public AuthOptions getAuthOptions() {
	return authOpts;
    }

}
