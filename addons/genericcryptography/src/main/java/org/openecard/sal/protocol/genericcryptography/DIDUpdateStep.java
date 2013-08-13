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

package org.openecard.sal.protocol.genericcryptography;

import iso.std.iso_iec._24727.tech.schema.DIDUpdate;
import iso.std.iso_iec._24727.tech.schema.DIDUpdateResponse;
import java.util.Map;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the DIDUpdate step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.3.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class DIDUpdateStep implements ProtocolStep<DIDUpdate, DIDUpdateResponse> {

    private static final Logger logger = LoggerFactory.getLogger(DIDUpdateStep.class);
    private final Dispatcher dispatcher;

    /**
     * Creates a new DIDUpdateStep.
     *
     * @param dispatcher Dispatcher
     */
    public DIDUpdateStep(Dispatcher dispatcher) {
	//TODO Implement me
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDUpdate;
    }

    @Override
    public DIDUpdateResponse perform(DIDUpdate request, Map<String, Object> internalData) {
	return WSHelper.makeResponse(DIDUpdateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

}
