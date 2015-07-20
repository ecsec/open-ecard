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

package org.openecard.crypto.common.asn1.eac;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Wrapper to bind {@link PACEInfo} and {@link PACEDomainParameterInfo} together.
 *
 * @author Tobias Wich
 */
public class PACESecurityInfoPair {

    private final PACEInfo pi;
    private final PACEDomainParameterInfo dpi;

    /**
     * Creates pair based on the given values.
     *
     * @param pi PACEInfo object.
     * @param dpi Domain Parameters. {@code null} for standard parameters.
     */
    public PACESecurityInfoPair(@Nonnull PACEInfo pi, @Nullable PACEDomainParameterInfo dpi) {
	this.pi = pi;
	this.dpi = dpi;
    }

    @Nonnull
    public PACEInfo getPACEInfo() {
	return pi;
    }

    @Nullable
    public PACEDomainParameterInfo getPACEDomainParameterInfo() {
	return dpi;
    }

    /**
     * Checks if there is a PACEDomainParameterInfo object associated with the PACEInfo object.
     * This also indicates whether the PACEInfo object uses standard domain paramaters or not.
     *
     * @return Returns true if there is a PACEDomainParameterInfo object, false otherwise.
     */
    public boolean hasPACEDomainParameterInfo() {
	return dpi != null;
    }

    /**
     * Creates a PACEDomainParameter object based on this pair.
     *
     * @return The new PACEDomainParameter object.
     */
    public PACEDomainParameter createPACEDomainParameter() {
	return new PACEDomainParameter(this);
    }

}
