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
import org.json.JSONObject;
import skid.mob.impl.client.InvalidServerData;


/**
 *
 * @author Tobias Wich
 */
public class RequestedAttribute {

    protected final DocumentContext jsonCtx;

    public RequestedAttribute(JSONObject jsonObj) {
	this.jsonCtx = JsonPath.parse(jsonObj);
    }

    public void load() throws InvalidServerData {
	
    }

    public String getName() {
	String val = jsonCtx.read("$.Name");
	return val;
    }

    public String getNameFormat() {
	String val = jsonCtx.read("$.NameFormat");
	return val;
    }

    public String getFriendlyName() {
	String val = jsonCtx.read("$.FriendlyName");
	return val;
    }

    public boolean isRequired() {
	boolean val = jsonCtx.read("$.isRequired");
	return val;
    }

}
