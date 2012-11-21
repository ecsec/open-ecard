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

package org.openecard.transport.httpcore;

import java.io.OutputStream;
import org.apache.http.impl.io.AbstractSessionOutputBuffer;
import org.apache.http.params.HttpParams;


/**
 * Stream based output buffer for use in Apache httpcore.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StreamSessionOutputBuffer extends AbstractSessionOutputBuffer {

    /**
     * Creates a StreamSessionOutputBuffer instance based on a given OutputStream.
     *
     * @param out The destination output stream.
     * @param bufsize The size of the internal buffer.
     * @param params HTTP parameters.
     */
    public StreamSessionOutputBuffer(OutputStream out, int bufsize, HttpParams params) {
	init(out, bufsize, params);
    }

}
