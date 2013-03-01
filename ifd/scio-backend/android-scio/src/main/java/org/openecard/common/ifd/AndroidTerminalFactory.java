/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.common.ifd;


/**
 * Extend the TerminalFactory with a start- and stop-method for android to take care of starting and stopping things
 * like the pcsc daemon, bluetooth or nfc.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public interface AndroidTerminalFactory extends TerminalFactory {

    /**
     * Stop daemons and services.
     */
    void stop();

    /**
     * Start daemons and services.
     *
     * @param o
     *            this will most likely be the application context
     */
    void start(Object o);

}
