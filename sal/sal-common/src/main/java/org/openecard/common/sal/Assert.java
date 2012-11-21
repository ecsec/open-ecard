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

package org.openecard.common.sal;

import java.util.Collection;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.exception.NamedEntityNotFoundException;
import org.openecard.common.sal.exception.SecurityConditionNotSatisfiedException;
import org.openecard.common.sal.state.CardStateEntry;


/**
 * Assertion convenience class for the SAL.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class Assert {

    /**
     * Checks if the given value is not empty (null), otherwise a NamedEntityNotFoundException is thrown.
     *
     * @param value Value
     * @param message Exception message
     * @throws NamedEntityNotFoundException
     */
    public static void assertNamedEntityNotFound(Object value, String message) throws NamedEntityNotFoundException {
	if (value == null) {
	    throw new NamedEntityNotFoundException(message);
	}
    }

    /**
     * Checks if the given value is not null, otherwise a IncorrectParameterException is thrown.
     *
     * @param value Value
     * @param message Exception message
     * @throws IncorrectParameterException
     */
    public static void assertIncorrectParameter(Object value, String message) throws IncorrectParameterException {
	if (value == null) {
	    throw new IncorrectParameterException(message);
	} else if (value instanceof Collection) {
	    if (((Collection) value).isEmpty()) {
		throw new IncorrectParameterException(message);
	    }
	}
    }

    /**
     * Checks the Application Security Condition.
     *
     * @param entry CardStateEntry
     * @param applicationID Application identifier
     * @param action Service action
     * @throws SecurityConditionNotSatisfiedException
     */
    public static void securityConditionApplication(CardStateEntry entry, byte[] applicationID, Enum<?> action)
	    throws SecurityConditionNotSatisfiedException {
	if (!entry.checkApplicationSecurityCondition(applicationID, action)) {
	    throw new SecurityConditionNotSatisfiedException();
	}
    }

    /**
     * Checks the Dataset Security Condition.
     *
     * @param entry CardStateEntry
     * @param applicationID Application identifier
     * @param dataSetName Dataset name
     * @param action Service action
     * @throws SecurityConditionNotSatisfiedException
     */
    public static void securityConditionDataSet(CardStateEntry entry, byte[] applicationID, String dataSetName, Enum<?> action)
	    throws SecurityConditionNotSatisfiedException {
	if (!entry.checkDataSetSecurityCondition(applicationID, dataSetName, action)) {
	    throw new SecurityConditionNotSatisfiedException();
	}
    }

    /**
     * Checks the DID Security Condition.
     *
     * @param entry CardStateEntry
     * @param applicationID Application identifier
     * @param didName DID name
     * @param action Service action
     * @throws SecurityConditionNotSatisfiedException
     */
    public static void securityConditionDID(CardStateEntry entry, byte[] applicationID, String didName, Enum<?> action)
	    throws SecurityConditionNotSatisfiedException {
	if (!entry.checkDIDSecurityCondition(applicationID, didName, action)) {
	    throw new SecurityConditionNotSatisfiedException();
	}
    }

}
