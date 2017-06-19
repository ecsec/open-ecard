/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 *
 * @author Tobias Wich
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "MiddlewareConfig")
@XmlType(name = "MiddlewareConfigType", propOrder = {
    "libName",
    "searchPath",
    "x86searchPath",
    "amd64searchPath"
})
public class MiddlewareConfig {

    @XmlElement(name = "LibName", required = true)
    private String libName;
    @XmlElement(name = "SearchPath")
    private ArrayList<String> searchPath;
    @XmlElement(name = "X86SearchPath")
    private ArrayList<String> x86searchPath;
    @XmlElement(name = "Amd64SearchPath")
    private ArrayList<String> amd64searchPath;

}
