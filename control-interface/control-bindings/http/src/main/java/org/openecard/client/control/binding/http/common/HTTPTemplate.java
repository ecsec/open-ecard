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

package org.openecard.client.control.binding.http.common;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import org.openecard.client.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class HTTPTemplate {

    private static final Logger _logger = LoggerFactory.getLogger(HTTPTemplate.class);
    private HashMap<String, String> properties = new HashMap<String, String>();
    private String content;

    /**
     * Creates a new HTTPTemplate.
     *
     * @param documentRoot Document root
     * @param templatePath Template path
     */
    public HTTPTemplate(DocumentRoot documentRoot, String templatePath) {
	try {
	    URL url = documentRoot.getFile(templatePath);
	    content = FileUtils.toString(url.openStream());
	} catch (Exception e) {
	    _logger.error(e.getMessage(), e);
	}
    }

    /**
     * Sets a property of the template.
     *
     * @param key Key
     * @param value Value
     */
    public void setProperty(String key, String value) {
	properties.put(key, value);
    }

    /**
     * Removes a property.
     *
     * @param key Key
     */
    public void removeProperty(String key) {
	properties.remove(key);
    }

    /**
     * Returns the template as a byte array.
     *
     * @return Template as a byte array
     * @throws UnsupportedEncodingException
     */
    public byte[] getBytes() throws UnsupportedEncodingException {
	return toString().getBytes("UTF-8");
    }

    @Override
    public String toString() {
	StringBuilder out = new StringBuilder(content);
	for (String key : properties.keySet()) {
	    int i = out.indexOf(key);
	    int j = i + key.length();
	    if (i > 0 && j > 0) {
		out.replace(i, j, properties.get(key));
	    }
	}
	return out.toString();
    }

}
