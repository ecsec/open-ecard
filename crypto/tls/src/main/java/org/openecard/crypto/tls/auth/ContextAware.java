/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of SkIDentity.
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.crypto.tls.auth;

import org.openecard.bouncycastle.crypto.tls.TlsContext;


/**
 * Interface which can be added to a class when it is necessary to give it's instance the TLS context information.
 *
 * @author Tobias Wich
 */
public interface ContextAware {

    /**
     * Sets the TLS context in this instance.
     * The context must match the currently running connection.
     *
     * @param context TLS context object of the currently open connection.
     */
    void setContext(TlsContext context);

}
