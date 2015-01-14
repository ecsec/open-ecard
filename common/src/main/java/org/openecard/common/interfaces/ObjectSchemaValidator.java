/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.common.interfaces;

import javax.annotation.Nonnull;


/**
 * Interface for schema based object validations.
 * One example of schema based validation is XML Schema validation for JAXB objects.
 *
 * @author Tobias Wich
 */
public interface ObjectSchemaValidator {

    /**
     * Validates the given object against the schema definition of the instance.
     *
     * @param obj The object to verify.
     * @return {@code true} if the object validates, {@code false} otherwise.
     * @throws ObjectValidatorException Indicates any error beyond the pure validation such as a failed conversion of
     *   the data.
     */
    boolean validateObject(@Nonnull Object obj) throws ObjectValidatorException;

}
