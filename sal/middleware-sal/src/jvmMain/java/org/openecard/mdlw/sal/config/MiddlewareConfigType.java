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

package org.openecard.mdlw.sal.config;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * XML-Schema for the Middleware Configuration
 *
 * @author Tobias Wich
 * @author Mike Prechtl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "MiddlewareConfig")
@XmlType(name = "MiddlewareConfigType", propOrder = {
    "middlewareSpecs"
})
public class MiddlewareConfigType {

    @XmlElement(name = "MiddlewareSpec")
    private final ArrayList<MiddlewareSpecType> middlewareSpecs = new ArrayList<>();

    public ArrayList<MiddlewareSpecType> getMiddlewareSpecs() {
        return middlewareSpecs;
    }

}
