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

package org.openecard.client.ws;

import java.io.IOException;
import java.util.Properties;
import org.openecard.client.common.OverridingProperties;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WsdefProperties {

    private static class Internal extends OverridingProperties {
        public Internal() throws IOException {
            super("wsdef.properties");
        }
    }

    static {
        try {
            properties = new Internal();
        } catch (IOException ex) {
            // in that case a null pointer occurs when properties is accessed
            org.slf4j.LoggerFactory.getLogger(WsdefProperties.class).error("Failed to load wsdef.properties file correctly.", ex);
        }
    }

    private static Internal properties;


    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static Object setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }

    public static Properties properties() {
        return properties.properties();
    }

}
