/****************************************************************************
 * Copyright (C) 2014-2019 ecsec GmbH.
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

package org.openecard.addon.bind;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.openecard.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Request specific Body for use in Plug-Ins and Bindings.
 * Additionally to the base elements, a request contains the requested resource.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class RequestBody extends Body {

    private static final Logger LOG = LoggerFactory.getLogger(RequestBody.class);

    /**
     * Resource path of the request.
     */
    private final String path;

    public RequestBody(String path) {
	super();
	this.path = path;
    }

    /**
     * Get the resource path of this request.
     *
     * @return A String containing the requests resource path.
     */
    public String getPath() {
	return path;
    }

    public Map<String, String> getFormParamValue() {
	Charset utf8 = StandardCharsets.UTF_8;
	Map<String, String> result = new HashMap<>();
	String value = StringUtils.nullToEmpty(getValueString()).trim();

	String[] entries = value.split("&");
	for (String entry : entries) {
	    String[] split = entry.split("=");
	    if (split.length == 1) {
		String key = URLDecoder.decode(split[0], utf8);
		result.put(key, null);
	    } else if (split.length == 2) {
		String key = URLDecoder.decode(split[0], utf8);
		String val = URLDecoder.decode(split[1], utf8);
		result.put(key, val);
	    }
	}

	return result;
    }

}
