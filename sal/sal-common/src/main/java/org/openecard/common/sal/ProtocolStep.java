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

import iso.std.iso_iec._24727.tech.schema.RequestType;
import iso.std.iso_iec._24727.tech.schema.ResponseType;
import java.util.Map;


/**
 * Interface which must be implemented to perform a step in the protocol.<br/>
 * The request and response parameters are parameterized, so that the actual type becomes apparent in the step.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface ProtocolStep <Request extends RequestType, Response extends ResponseType> {

    /**
     * Get the type of SAL function this step can be applied to.
     * @return SAL function type.
     */
    FunctionType getFunctionType();

    /**
     * @param request Request object from the actual SAL function.
     * @param internalData Map with objects from previous steps.
     * @return Response object for the actual SAL function.
     */
    Response perform(Request request, Map<String,Object> internalData);

}
