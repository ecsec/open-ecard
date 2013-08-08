/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * 
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
@XmlRootElement(name = "Configuration")
public class Configuration {

    private final List<ConfigurationEntry> entries = new ArrayList<ConfigurationEntry>();

    @XmlElements({
	    @XmlElement(name = "EnumEntry", type = EnumEntry.class),
	    @XmlElement(name = "EnumListEntry", type = EnumListEntry.class),
	    @XmlElement(name = "ScalarEntry", type = ScalarEntry.class),
	    @XmlElement(name = "ScalarListEntry", type = ScalarListEntry.class) })
    @XmlElementWrapper(name = "Entries")
    public List<ConfigurationEntry> getEntries() {
	return entries;
    }

}
