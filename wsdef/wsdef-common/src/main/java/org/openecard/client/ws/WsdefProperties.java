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

package org.openecard.client.ws;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            Logger.getLogger(WsdefProperties.class.getName()).log(Level.SEVERE, null, ex);
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
