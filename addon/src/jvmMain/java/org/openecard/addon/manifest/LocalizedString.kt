/****************************************************************************
 * Copyright (C) 2013-2014 HS Coburg.
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
 */
package org.openecard.addon.manifest

import jakarta.xml.bind.annotation.*

/**
 *
 * @author Dirk Petrautzki
 */
@XmlRootElement(name = "LocalizedString")
@XmlAccessorType(XmlAccessType.FIELD)
class LocalizedString {
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    var lang: String? = null

    @XmlValue
    var value: String? = null
}
