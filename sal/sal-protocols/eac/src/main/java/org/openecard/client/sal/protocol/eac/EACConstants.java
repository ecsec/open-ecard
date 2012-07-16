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

package org.openecard.client.sal.protocol.eac;

/**
 * Defines constants for the EAC protocol.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EACConstants {

    // EF.CardSecurity file identifier
    public static final short EF_CARDSECURITY_FID = (short) 0x011D;
    // Internal data
    protected static final String INTERNAL_DATA_CERTIFICATES = "Certificates";
    protected static final String INTERNAL_DATA_AUTHENTICATED_AUXILIARY_DATA = "AuthenticatedAuxiliaryData";
    protected static final String INTERNAL_DATA_PK_PCD = "PKPCD";
    protected static final String INTERNAL_DATA_SECURITY_INFOS = "SecurityInfos";
    protected static final String INTERNAL_DATA_CARD_STATE_ENTRY = "cardState";
    protected static final String INTERNAL_DATA_CURRENT_CAR = "CurrentCAR";

}
