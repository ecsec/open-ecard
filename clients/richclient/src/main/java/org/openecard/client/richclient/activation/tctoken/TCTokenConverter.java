/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.richclient.activation.tctoken;

import java.io.IOException;


/**
 * Remove the converter as soon as possible!!!
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
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
	} catch (Exception e) {
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
