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

package org.openecard.ifd.protocol.pace.crypto;

import org.openecard.crypto.common.asn1.eac.PACEDomainParameter;


/**
 * Implements the Integrated Mapping for PACE.
 * See BSI-TR-03110, version 2.10, part 3, section A.3.5.2.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class PACEIntegratedMapping extends PACEMapping {

    /**
     * Creates an new integrated mapping for PACE.
     *
     * @param pdp PACEDomainParameter
     */
    public PACEIntegratedMapping(PACEDomainParameter pdp) {
	super(pdp);
    }

    @Override
    public PACEDomainParameter map(byte[] keyPICC, byte[] keyPCD) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
