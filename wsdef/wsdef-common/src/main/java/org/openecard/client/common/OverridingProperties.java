/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;


/**
 * Basic Properties class which overrides default values from System.properties on its creation.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class OverridingProperties {

    private final Properties properties;

    protected OverridingProperties(String fName) throws IOException {
	InputStream in = OverridingProperties.class.getResourceAsStream("/" + fName);
	if (in == null) {
	    in = OverridingProperties.class.getResourceAsStream(fName);
	}
        Reader r = new InputStreamReader(in, "utf-8");
	properties = new Properties();
	properties.load(in);
	init();
    }
    protected OverridingProperties(InputStream stream) throws IOException {
	properties = new Properties();
	properties.load(stream);
	init();
    }
    protected OverridingProperties(Properties props) {
	properties = new Properties(props);
	init();
    }

    private void init() {
	overrideFromSystem();
    }

    private void overrideFromSystem() {
	for (Object nextKey : properties.keySet()) {
	    if (nextKey instanceof String) {
		String next = (String) nextKey;
		if (System.getProperties().containsKey(next)) {
		    properties.setProperty(next, System.getProperties().getProperty(next));
		}
	    }
	}
    }


    public final String getProperty(String key) {
	return properties.getProperty(key);
    }

    public final Object setProperty(String key, String value) {
	return properties.setProperty(key, value);
    }

    public final Properties properties() {
        return new Properties(properties);
    }

}
