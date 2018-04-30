/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.common.util;

import javax.annotation.Nonnull;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.w3c.dom.Document;


/**
 *
 * @author Tobias Wich
 */
public class JAXBUtils {

    @Nonnull
    public static <T> T deepCopy(@Nonnull T in) throws MarshallingTypeException, WSMarshallerException {
	WSMarshaller m = WSMarshallerFactory.createInstance();
	Document d = m.marshal(in);
	Object out = m.unmarshal(d);
	return ((Class<T>) in.getClass()).cast(out);
    }

}
