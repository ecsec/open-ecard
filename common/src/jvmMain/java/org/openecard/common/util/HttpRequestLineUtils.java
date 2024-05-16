/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


/**
 * Utility class to transform various aspects of an HTTP request line into more usable data structures.
 *
 * @author Tobias Wich
 */
public class HttpRequestLineUtils {

    /**
     * Transform query parameters into a java map. The parameters are not decoded, but taken as is. The query string has
     * the form <pre>key(=value)?&amp;key((=value)?)*</pre>. If a key does not have a value, null is taken as value.
     *
     * @param queryStr Query string as found in the HTTP request line.
     * @return Map with key value pairs of the query parameters.
     */
    public static Map<String, String> transformRaw(String queryStr) {
	HashMap<String, String> result = new HashMap<>();

	if (queryStr != null) {
	    String[] queries = queryStr.split("&");
	    for (String query : queries) {
		// everything in front of the equal sign is the key
		// everything behind the equal sign is the value
		int first = query.indexOf('=');
		if (first == -1) {
		    result.put(query, "");
		} else {
		    String key = query.substring(0, first);
		    String value = query.substring(first + 1, query.length());
		    result.put(key, value);
		}
	    }
	}

	return result;
    }

    /**
     * Simplification of {@link #transform(String, String)} with the default encoding UTF-8.
     *
     * @param queryStr Query string as found in the HTTP request line.
     * @return Map with key value pairs of the query parameters.
     * @throws UnsupportedEncodingException Thrown if the strings decoded from the URL encoded value have a different
     *   encoding than UTF-8.
     */
    public static Map<String, String> transform(String queryStr) throws UnsupportedEncodingException {
	return transform(queryStr, "UTF-8");
    }

    /**
     * Transform query parameters into a java map and URL decode the values.
     * The parameters are not decoded, but taken as is. The query string has the form
     * <pre>key(=value)?&amp;key((=value)?)*</pre>. If a key does not have a value, null is taken as value. The resulting
     * values are encoded according to the given encoding
     *
     * @param queryStr Query string as found in the HTTP request line.
     * @param encoding Encoding used in the {@link URLDecoder#decode(String, String)} function.
     * @return Map with key value pairs of the query parameters.
     * @throws UnsupportedEncodingException Thrown if the strings decoded from the URL encoded value have a different
     *   encoding than the one defined in this function.
     */
    public static Map<String, String> transform(String queryStr, String encoding) throws UnsupportedEncodingException {
	// copy the raw strings
	Map<String, String> resultRaw = transformRaw(queryStr);
	HashMap<String, String> result = new HashMap<>();
	for (Map.Entry<String, String> next : resultRaw.entrySet()) {
	    String k = next.getKey();
	    String v = next.getValue();
	    // URL decode both values
	    k = decodeValue(k, encoding);
	    v = decodeValue(v, encoding);
	    result.put(k, v);
	}

	return result;
    }

    private static String decodeValue(String v, String encoding) throws UnsupportedEncodingException {
	if (v != null) {
	    // handle + special, because the decoder does not perform this
	    v = v.replace("+", " ");
	    v = URLDecoder.decode(v, encoding);
	}
	return v;
    }

}
