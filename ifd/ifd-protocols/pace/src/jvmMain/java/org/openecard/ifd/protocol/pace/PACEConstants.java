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

package org.openecard.ifd.protocol.pace;

import java.util.Arrays;
import java.util.List;
import org.openecard.crypto.common.asn1.eac.oid.PACEObjectIdentifier;


/**
 * Defines constants for the PACE protocol.
 *
 * @author Moritz Horsch
 */
class PACEConstants {

    // EF.CardAccess file identifier
    public static final short EF_CARDACCESS_FID = (short) 0x011C;
	public static final byte EF_CARDACCESS_FID_SHORT = (byte) 0x1C;
    // PACE password types
    public static final byte PASSWORD_MRZ = (byte) 0x01;
    public static final byte PASSWORD_CAN = (byte) 0x02;
    public static final byte PASSWORD_PIN = (byte) 0x03;
    public static final byte PASSWORD_PUK = (byte) 0x04;
    public static final String PIN_CHARSET = "ISO-8859-1";
    // MSE:Set AT error handling
    public static final short PASSWORD_SUSPENDED = (short) 0x63C1;
    public static final short PASSWORD_BLOCKED = (short) 0x63C0;
    public static final short PASSWORD_ERROR = (short) 0x6982;
    public static final short PASSWORD_DEACTIVATED = (short) 0x6283;
    // General Authenticate error handling
    public static final short SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    public static final short AUTHENTICATION_METHOD_BLOCKED = (short) 0x6983;
    public static final short REFERENCE_DATA_NOT_USABLE = (short) 0x6984;
    public static final short CONDITIONS_OF_USE_NOT_SATISFIED = (short) 0x6985;
    public static final short CMD_FAILED = (short) 0x6300;
    public static final short INCORRECT_PARA = (short) 0x6300;

	public static final List<String> SUPPORTED_PACE_PROTOCOLS = Arrays.asList(
		PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128,
		PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_192,
		PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_256
	);
    public static final List<Integer> SUPPORTED_PACE_DOMAIN_PARAMS = Arrays.asList(
	    10, // NIST P-224 (secp224r1)
	    11, // BrainpoolP224r1
	    12, // NIST P-256 (secp256r1)
	    13, // BrainpoolP256r1
	    14, // BrainpoolP320r1
	    15, // NIST P-384 (secp384r1)
	    16, // BrainpoolP384r1
	    17, // BrainpoolP512r1
	    18  // NIST P-521 (secp521r1)
    );

}
