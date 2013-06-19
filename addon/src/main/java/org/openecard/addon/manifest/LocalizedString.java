/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

package org.openecard.addon.manifest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class LocalizedString {

    private String value;
    private String lang;

    public String getValue() {
	return value;
    }

    @XmlValue
    public void setValue(String value) {
	this.value = value;
    }

    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    public String getLang() {
	return lang;
    }

    public void setLang(String value) {
	this.lang = value;
    }

}
