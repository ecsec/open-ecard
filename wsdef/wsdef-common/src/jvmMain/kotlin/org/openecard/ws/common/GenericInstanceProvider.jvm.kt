/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.ws.common

/**
 * Generic factory capable of creating instances for a type.
 *
 * @param T Type the factory creates instances for.
 *
 * @author Tobias Wich
 * @author Neil Crossley
 */
interface GenericInstanceProvider<T> {
	@get:Throws(GenericFactoryException::class)
    val instance: T
}
