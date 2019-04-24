/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.android.utils;


/**
 * Result of a NFC capability request such as extended length support.
 *
 * @author Tobias Wich
 */
public enum NfcCapabilityResult {

    /** The requested capability is supported by the system. */
    SUPPORTED,
    /** The requested capability is not supported by the system. */
    NOT_SUPPORTED,
    /** The capability query is not allowed to be performed on this system. */
    QUERY_NOT_ALLOWED,
    /** Query could not be performed because the NFC subsystem is disabled. */
    NFC_SYSTEM_DISABLED;

}
