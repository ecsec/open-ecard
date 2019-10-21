/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
package org.openecard.mobile.activation.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class ArrayBackedAutoCloseable implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ArrayBackedAutoCloseable.class);
    private static final String errorMessage = "An exception occurred while closing a resource.";

    private final AutoCloseable[] delegates;

    public ArrayBackedAutoCloseable(AutoCloseable[] delegates) {
	this.delegates = new AutoCloseable[delegates.length];
	System.arraycopy(delegates, 0, this.delegates, 0, delegates.length);
    }

    @Override
    public void close() throws Exception {
	Exception firstException = null;

	for (int i = 0; i < this.delegates.length; i++) {
	    AutoCloseable closeable = this.delegates[i];
	    try {
		closeable.close();
	    } catch(Exception e) {
		if (firstException != null) {
		    firstException = e;
		}
		LOG.debug(errorMessage, e);
	    }
	}
	throw new RuntimeException(errorMessage, firstException);
    }

}
