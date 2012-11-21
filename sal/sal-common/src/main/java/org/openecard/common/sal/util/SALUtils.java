/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.common.sal.util;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import java.lang.reflect.Method;
import java.util.Map;
import org.openecard.common.ECardException;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.exception.NamedEntityNotFoundException;
import org.openecard.common.sal.exception.UnknownConnectionHandleException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;


/**
 * Convenience class for the SAL.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SALUtils {

    public static ConnectionHandleType getConnectionHandle(Object object) throws ECardException, Exception {
	ConnectionHandleType value = (ConnectionHandleType) get(object, "getConnectionHandle");
	Assert.assertIncorrectParameter(value, "The parameter ConnectionHandle is empty.");

	return value;
    }

    public static String getDIDName(Object object) throws ECardException, Exception {
	String value = (String) get(object, "getDIDName");
	Assert.assertIncorrectParameter(value, "The parameter DIDName is empty.");

	return value;
    }

    public static DIDStructureType getDIDStructure(Object object, String didName, CardStateEntry entry, ConnectionHandleType connectionHandle)
	    throws NamedEntityNotFoundException, Exception {
	DIDScopeType didScope = (DIDScopeType) get(object, "getDIDScope");
	DIDStructureType didStructure;

	if (didScope != null && didScope.equals(DIDScopeType.GLOBAL)) {
	    didStructure = entry.getDIDStructure(didName, entry.getImplicitlySelectedApplicationIdentifier());
	} else {
	    didStructure = entry.getDIDStructure(didName, connectionHandle.getCardApplication());
	}

	Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	return didStructure;
    }

    public static CardStateEntry getCardStateEntry(CardStateMap states, ConnectionHandleType connectionHandle)
	    throws UnknownConnectionHandleException {
	CardStateEntry value = states.getEntry(connectionHandle);
	if (value == null) {
	    throw new UnknownConnectionHandleException(connectionHandle);
	}

	return value;
    }

    public static CardStateEntry getCardStateEntry(Map<String, Object> internalData, ConnectionHandleType connectionHandle)
	    throws UnknownConnectionHandleException {
	CardStateEntry value = (CardStateEntry) internalData.get("cardState");
	if (value == null) {
	    throw new UnknownConnectionHandleException(connectionHandle);
	}

	return value;
    }

    private static Object get(Object object, String method) throws Exception {
	Method[] methodes = object.getClass().getDeclaredMethods();

	for (Method m : methodes) {
	    if (m.getName().equals(method)) {
		return m.invoke(object);
	    }
	}

	return null;
    }

}
