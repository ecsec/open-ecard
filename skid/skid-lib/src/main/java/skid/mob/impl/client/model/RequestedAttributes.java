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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import skid.mob.impl.client.InvalidServerData;


/**
 *
 * @author Tobias Wich
 */
public class RequestedAttributes {

    private final JSONArray jsonObj;
    private List<RequestedAttribute> reqAttrs;

    public RequestedAttributes(JSONArray jsonObj) {
	this.jsonObj = jsonObj;
    }

    public void load() throws InvalidServerData {
	reqAttrs = new ArrayList<>();

	for (int i = 0; i < jsonObj.length(); i++) {
	    Object next = jsonObj.opt(i);
	    if (next instanceof JSONObject) {
		RequestedAttribute nextAttr = new RequestedAttribute((JSONObject) next);
		nextAttr.load();
		reqAttrs.add(nextAttr);
	    } else {
		throw new InvalidServerData("Invalid RequestedAttribute found in AuthOption block.");
	    }
	}
    }

    public List<RequestedAttribute> getReqAttrs() {
	return Collections.unmodifiableList(reqAttrs);
    }

}
