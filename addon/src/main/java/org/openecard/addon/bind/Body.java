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

package org.openecard.addon.bind;

import javax.xml.bind.JAXBElement;
import org.w3c.dom.Node;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Body {

    private String _mIMEType;
    private Node _value;

    public void setValue(JAXBElement aObj) {
	throw new UnsupportedOperationException();
    }

    public void setValue(Node aXml) {
	throw new UnsupportedOperationException();
    }

    public void setValue(String aData) {
	throw new UnsupportedOperationException();
    }

    public void setValue(Number aNum) {
	throw new UnsupportedOperationException();
    }

}
