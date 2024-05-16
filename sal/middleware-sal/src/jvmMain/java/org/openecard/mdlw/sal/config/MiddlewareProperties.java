/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.mdlw.sal.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class MiddlewareProperties {

    private static final Logger LOG = LoggerFactory.getLogger(MiddlewareProperties.class);
    private static MiddlewareProperties INST;

    private final Properties props;

    private MiddlewareProperties() {
	this.props = new Properties();
    }

    private MiddlewareProperties(String propsResource) throws IOException {
	InputStream is = getClass().getResourceAsStream(propsResource);
	this.props = new Properties();
	this.props.load(is);
    }

    private static synchronized MiddlewareProperties getInstance() {
	if (INST == null) {
	    try {
		INST = new MiddlewareProperties("/middleware/mw.properties");
	    } catch (IOException ex) {
		LOG.error("The bundled properties file could not be loaded.", ex);
		INST = new MiddlewareProperties();
	    }
	}
	return INST;
    }

    public static boolean isLoadExternalModules() {
	String val = getInstance().props.getProperty("modules.load-external", "true");
	return Boolean.parseBoolean(val);
    }

    public static boolean isForceLoadInternalModules() {
	String val = getInstance().props.getProperty("modules.internal.force-enable", "false");
	return Boolean.parseBoolean(val);
    }

}
