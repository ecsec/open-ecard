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

package org.openecard.client.connector.handler.tctoken;

import java.io.IOException;


/**
 * Remove the converter as soon as possible!!!
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenConverter {

    private String data;

    public String convert(String input) {
	int x = input.indexOf("<object");
	int y = input.indexOf("object", x + 7);
	data = input.substring(x, y);

	StringBuilder out = new StringBuilder();
	out.append("<TCTokenType>");
	try {
	    while (true) {
		out.append(convertParameter(data));
	    }
	} catch (Exception ignore) {
	}
	out.append("</TCTokenType>");

	return out.toString();
    }

    private String convertParameter(String input) throws IOException {
	StringBuilder out = new StringBuilder();

	int x = input.indexOf("<param name=");
	if (x == -1) {
	    throw new IOException();
	} else {
	    x += 13;
	}
	String element = input.substring(x, input.indexOf("\"", x));
	// fix for non-conforming PathSecurity-Parameters element
	if (!element.equals("PathSecurity-Parameters")) {
	    element = element.replace("PathSecurity-Parameter", "PathSecurity-Parameters");
	}

	int y = input.indexOf("value=", x) + 7;
	String value = input.substring(y, input.indexOf("\"", y));

	out.append("<");
	out.append(element);
	out.append(">");
	out.append(value);
	out.append("</");
	out.append(element);
	out.append(">");

	data = input.substring(y + value.length(), input.length());

	return out.toString();
    }

}
