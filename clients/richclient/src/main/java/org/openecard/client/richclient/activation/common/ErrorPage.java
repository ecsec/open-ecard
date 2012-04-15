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
package org.openecard.client.richclient.activation.common;


/**
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ErrorPage {

    private String page;

    public ErrorPage(String message) {
	page = generatePage("Error", message);
    }

    public ErrorPage(String title, String message) {
	page = generatePage(title, message);
    }

    private String generatePage(String title, String message) {
	StringBuilder sb = new StringBuilder();

	sb.append("<html>");
	sb.append("<head>");
	sb.append("</head>");
	sb.append("<body>");
	sb.append(generateCSS());
	sb.append("<h1>");
	sb.append(title);
	sb.append("</h1>");
	sb.append(message);
	sb.append("</body>");
	sb.append("</html>");

	return sb.toString();
    }

    private String generateCSS() {
	StringBuilder sb = new StringBuilder();

	sb.append("<style>");
	sb.append("html {background-color:#eaeaea;font-family:Arial;font-size:12px; line-height: 18px;}");
	sb.append("body {height: 200px; width: 500px; margin: 100px auto; background-color:#fff; border: 1px solid #ccc; padding: 20px;}");
	sb.append("</style>");

	return sb.toString();
    }

    public String getHTML() {
	return page;
    }

}
