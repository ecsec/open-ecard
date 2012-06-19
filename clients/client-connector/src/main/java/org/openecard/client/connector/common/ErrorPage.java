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

package org.openecard.client.connector.common;


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
