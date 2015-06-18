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

import java.io.InputStream;


/**
 *
 * @author Tobias Wich
 */
public class Attachment {

    private String _mIMEType;
    private String _encoding;
    private InputStream _value;
    private String _name;
    private String _filename;

    public String getMIMEType() {
	return this._mIMEType;
    }

    public String getEncoding() {
	return this._encoding;
    }

    public InputStream getValue() {
	return this._value;
    }

}
