/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
*
* @author Tobias Wich
* @author Dirk Petrautzki
* @author Hans-Martin Haase
*/
@XmlRootElement(name = "ScalarListEntry")
@XmlType(propOrder = { "key", "type", "localizedName", "localizedDescription" })
public class ScalarListEntry extends ConfigurationEntry {

    @XmlElement(name = "Type", required = true)
    private ScalarListEntryType type;

    public String getType() {
	return type.name();
    }

    public void setType(String type) {
	this.type = ScalarListEntryType.valueOf(type);
    }
}
