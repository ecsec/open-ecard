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
public class AuthOptions {

    private final JSONArray jsonObj;
    private List<AuthOption> authOptions;
    private boolean loaded = false;

    public AuthOptions(JSONArray jsonObj) {
	this.jsonObj = jsonObj;
    }

    public synchronized void load() throws InvalidServerData {
	if (! loaded) {
	    authOptions = new ArrayList<>();

	    for (int i = 0; i < jsonObj.length(); i++) {
		Object next = jsonObj.opt(i);
		if (next instanceof JSONObject) {
		    AuthOption nextOption = new AuthOption((JSONObject) next);
		    nextOption.load();
		    authOptions.add(nextOption);
		} else {
		    throw new InvalidServerData("Invalid AuthenticationOption found in Accepts block.");
		}
	    }

	    loaded = true;
	}
    }

    public List<AuthOption> list() {
	return Collections.unmodifiableList(authOptions);
    }

}
