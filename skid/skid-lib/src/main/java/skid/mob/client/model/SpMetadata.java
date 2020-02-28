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
import com.jayway.jsonpath.JsonPath;


/**
 *
 * @author Tobias Wich
 */
public class SpMetadata {

    private final DocumentContext jsonCtx;

    public SpMetadata(Object jsonObj) {
	this.jsonCtx = JsonPath.parse(jsonObj);
    }

    public UiInfo getUiInfo() {
	// TODO: add error handling
	Object uiItemArray = jsonCtx.read("$.value.roleDescriptorOrIDPSSODescriptorOrSPSSODescriptor[0].SPSSODescriptor.Extensions.any[?(@.name == '{urn:oasis:names:tc:SAML:metadata:ui}UIInfo')].value.displayNameOrDescriptionOrKeywords.*");
	UiInfo uii = new UiInfo(uiItemArray);
	return uii;
    }

}
