/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


/**
 * Basic Properties class which overrides default values from System.properties on its creation.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class OverridingProperties {

    private final Properties properties;

    public OverridingProperties(String fName) throws IOException {
	InputStream in = this.getClass().getResourceAsStream("/" + fName);
	if (in == null) {
	    in = this.getClass().getResourceAsStream(fName);
	}
	properties = new Properties();
	properties.load(in);
	init();
    }
    public OverridingProperties(InputStream stream) throws IOException {
	properties = new Properties();
	properties.load(stream);
	init();
    }
    public OverridingProperties(Properties props) {
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


    public URL getDependentResource(String fname) {
	URL url = this.getClass().getResource("/" + fname);
	if (url == null) {
	    url = this.getClass().getResource(fname);
	}
	return url;
    }

    public final String getProperty(String key) {
	return properties.getProperty(key);
    }

    public final Object setProperty(String key, String value) {
	return properties.setProperty(key, value);
    }

    public final Properties properties() {
	return (Properties) properties.clone();
    }

}
