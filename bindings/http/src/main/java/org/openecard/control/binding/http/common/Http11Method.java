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

package org.openecard.control.binding.http.common;


/**
 * HTTP/1.1 methods
 *
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public enum Http11Method {

    GET("GET"),
    POST("POST"),
    HEAD("HEAD"),
    PUT("PUT"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    CONNECT("CONNECT");

    private String methodString;

    Http11Method(String methodString) {
	this.methodString = methodString;
    }

    /**
     * @return HTTP method name as string
     */
    public String getMethodString() {
	return this.methodString;
    }

}
