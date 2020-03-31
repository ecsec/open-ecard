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
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JsonOrgJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JsonOrgMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import java.util.EnumSet;
import java.util.Set;


/**
 *
 * @author Tobias Wich
 */
public class JsonConfig {

    private static boolean INIT = false;

    public static void assertInitialized() {
	synchronized (JsonConfig.class) {
	    if (!INIT) {
		Configuration.setDefaults(new Configuration.Defaults() {

		    private final JsonProvider jsonProvider = new JsonOrgJsonProvider();
		    private final MappingProvider mappingProvider = new JsonOrgMappingProvider();

		    @Override
		    public JsonProvider jsonProvider() {
			return jsonProvider;
		    }

		    @Override
		    public MappingProvider mappingProvider() {
			return mappingProvider;
		    }

		    @Override
		    public Set<Option> options() {
			return EnumSet.of(Option.SUPPRESS_EXCEPTIONS);
		    }
		});
		INIT = true;
	    }
	}
    }

}
