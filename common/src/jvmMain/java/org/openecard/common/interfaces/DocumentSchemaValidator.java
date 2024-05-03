/****************************************************************************
 * Copyright (C) 2015-2018 ecsec GmbH.
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Interface for schema based document validations.
 *
 * @author Tobias Wich
 */
public interface DocumentSchemaValidator {

    /**
     * Validates the given document against the schema definition of the instance.
     *
     * @param doc The document to verify.
     * @throws DocumentValidatorException Indicates a failed document validation.
     */
    void validate(@Nonnull Document doc) throws DocumentValidatorException;

    /**
     * Validates the given document element against the schema definition of the instance.
     *
     * @param doc The element to verify.
     * @throws DocumentValidatorException Indicates a failed document validation.
     */
    void validate(@Nonnull Element doc) throws DocumentValidatorException;

}
