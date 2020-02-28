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
public class UiInfo {

    private final DocumentContext jsonCtx;

    public UiInfo(Object jsonObj) {
	this.jsonCtx = JsonPath.parse(jsonObj);
    }

    public String displayName() {
	return null;
    }

    public String informationUrl() {
	return null;
    }

    public String privacyStatementUrl() {
	return null;
    }

    public String logoUrl() {
	return null;
    }

}
