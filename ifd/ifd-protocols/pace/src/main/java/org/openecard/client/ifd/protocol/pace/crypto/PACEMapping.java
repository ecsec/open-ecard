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
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ***************************************************************************/

package org.openecard.client.ifd.protocol.pace.crypto;

import org.openecard.client.crypto.common.asn1.eac.PACEDomainParameter;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class PACEMapping {

    /**
     * Stores the PACEDomainParameter.
     */
    protected PACEDomainParameter pdp;

    /**
     * Creates an new mapping for PACE.
     *
     * @param pdp PACEDomainParameter
     */
    protected PACEMapping(PACEDomainParameter pdp) {
	this.pdp = pdp;
    }

    /**
     * Perform the PACE mapping.
     *
     * @param keyPICC Key from PICC
     * @param keyPCD Key from PCD
     * @return PACEDomainParameter
     */
    public abstract PACEDomainParameter map(byte[] keyPICC, byte[] keyPCD);

}
