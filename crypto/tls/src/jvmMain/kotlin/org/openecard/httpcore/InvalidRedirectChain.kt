/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.httpcore


/**
 * Exception indicating a constraint violation or an error while following redirects.
 *
 * @author Tobias Wich
 */
class InvalidRedirectChain(msg: String, cause: Throwable? = null) : InvalidUrlException(msg, cause)
