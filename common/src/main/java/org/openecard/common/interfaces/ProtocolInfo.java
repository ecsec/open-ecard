/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

import java.util.List;


/**
 * Information interface for the SAL protocol registry.
 * It lists all available protocols and provides a lookup function.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface ProtocolInfo {

    /**
     * Checks if the map contains the given protocol URI.
     *
     * @param protocolURI Protocol URI
     * @return
     */
    public boolean contains(String protocolURI);

    /**
     * Returns a list of protocol URIs.
     *
     * @return List of protocol URIs
     */
    public List<String> protocols();

}
