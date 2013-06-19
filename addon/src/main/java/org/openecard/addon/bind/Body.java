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


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class Body {

    private String mimeType;
    @Deprecated
    private byte[] value;

    public Body(byte[] value, String mimeType) {
	this.value = value;
	this.mimeType = mimeType;
    }

    @Deprecated
    public byte[] getValue() {
	return value;
    }

    public void setValue(JAXBElement<?> jaxbElement) {
	throw new UnsupportedOperationException();
    }

    public void setValue(String data) {
	throw new UnsupportedOperationException();
    }

    public void setValue(Number num) {
	throw new UnsupportedOperationException();
    }

    public String getMimeType() {
	return mimeType;
    }

}
