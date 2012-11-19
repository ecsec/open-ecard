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

package org.openecard.client.sal.protocol.genericcryptography;

import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import java.util.Map;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the VerifyCertificate step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.11.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class VerifyCertificateStep implements ProtocolStep<VerifyCertificate, VerifyCertificateResponse> {

    private static final Logger logger = LoggerFactory.getLogger(VerifyCertificate.class);
    private final Dispatcher dispatcher;

    /**
     * Creates a new VerifyCertificateStep.
     *
     * @param dispatcher Dispatcher
     */
    public VerifyCertificateStep(Dispatcher dispatcher) {
        //TODO Implement me
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.VerifyCertificate;
    }

    @Override
    public VerifyCertificateResponse perform(VerifyCertificate request, Map<String, Object> internalData) {
	return WSHelper.makeResponse(VerifyCertificateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

}
