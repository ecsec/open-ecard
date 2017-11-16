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

package org.openecard.android.lib;


/**
 * Contains constants for the Open eCard Service.
 *
 * @author Mike Prechtl
 */
public class ServiceContextConstants {

    public static final String IFD_FACTORY_KEY = "org.openecard.ifd.scio.factory.impl";
    public static final String IFD_FACTORY_VALUE = "org.openecard.scio.NFCFactory";
    public static final String WSDEF_MARSHALLER_KEY = "org.openecard.ws.marshaller.impl";
    public static final String WSDEF_MARSHALLER_VALUE = "org.openecard.ws.android.AndroidMarshaller";

}
